package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.config.GitServerProperties;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.dto.CommitDTO;
import com.githubx.githubrepositoryms.dto.CommitDTO.AuthorDTO;
import com.githubx.githubrepositoryms.dto.CommitDTO.FileChangeDTO;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitServiceImpl implements CommitService {

    private final RepositoryDao repositoryDao;
    private final GitServerProperties props;

    @Override
    public List<CommitDTO> listCommits(String owner, String repo, String branch, String path, int page, int perPage) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return List.of();
        }

        RepositoryDocument repoDoc = repoOpt.get();
        String targetBranch = branch != null ? branch : repoDoc.getDefaultBranch();
        String remoteUrl = remoteUrl(owner, repo);
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("git-commits-");

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBare(true)
                    .setCloneAllBranches(true)
                    .call()) {

                Repository repository = git.getRepository();
                ObjectId branchId = repository.resolve("refs/heads/" + targetBranch);

                if (branchId == null) {
                    log.warn("Branch {} not found in {}/{}", targetBranch, owner, repo);
                    return List.of();
                }

                List<CommitDTO> commits = new ArrayList<>();
                int skip = (page - 1) * perPage;
                int count = 0;

                try (RevWalk revWalk = new RevWalk(repository)) {
                    revWalk.markStart(revWalk.parseCommit(branchId));

                    // Filter by path if specified
                    if (path != null && !path.isEmpty()) {
                        revWalk.setTreeFilter(PathFilter.create(path));
                    }

                    for (RevCommit commit : revWalk) {
                        if (count >= skip && commits.size() < perPage) {
                            commits.add(toCommitDTO(commit, false));
                        }
                        count++;
                        if (commits.size() >= perPage) {
                            break;
                        }
                    }
                }

                return commits;
            }

        } catch (GitAPIException | IOException e) {
            log.error("Failed to list commits for {}/{}: {}", owner, repo, e.getMessage(), e);
            return List.of();
        } finally {
            deleteTempDir(tempDir);
        }
    }

    @Override
    public CommitDTO getCommit(String owner, String repo, String sha) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return null;
        }

        String remoteUrl = remoteUrl(owner, repo);
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("git-commit-detail-");

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBare(true)
                    .setCloneAllBranches(true)
                    .call()) {

                Repository repository = git.getRepository();
                ObjectId commitId = repository.resolve(sha);

                if (commitId == null) {
                    log.warn("Commit {} not found in {}/{}", sha, owner, repo);
                    return null;
                }

                try (RevWalk revWalk = new RevWalk(repository)) {
                    RevCommit commit = revWalk.parseCommit(commitId);
                    CommitDTO dto = toCommitDTO(commit, true);

                    // Get file changes
                    List<FileChangeDTO> files = getFileChanges(repository, commit);
                    dto.setFiles(files);

                    // Calculate totals
                    int totalAdditions = files.stream().mapToInt(FileChangeDTO::getAdditions).sum();
                    int totalDeletions = files.stream().mapToInt(FileChangeDTO::getDeletions).sum();
                    dto.setAdditions(totalAdditions);
                    dto.setDeletions(totalDeletions);
                    dto.setFilesChanged(files.size());

                    return dto;
                }
            }

        } catch (GitAPIException | IOException e) {
            log.error("Failed to get commit {} for {}/{}: {}", sha, owner, repo, e.getMessage(), e);
            return null;
        } finally {
            deleteTempDir(tempDir);
        }
    }

    @Override
    public int countCommits(String owner, String repo, String branch) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return 0;
        }

        RepositoryDocument repoDoc = repoOpt.get();
        String targetBranch = branch != null ? branch : repoDoc.getDefaultBranch();
        String remoteUrl = remoteUrl(owner, repo);
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("git-count-commits-");

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBare(true)
                    .setCloneAllBranches(true)
                    .call()) {

                Repository repository = git.getRepository();
                ObjectId branchId = repository.resolve("refs/heads/" + targetBranch);

                if (branchId == null) {
                    return 0;
                }

                int count = 0;
                try (RevWalk revWalk = new RevWalk(repository)) {
                    revWalk.markStart(revWalk.parseCommit(branchId));
                    for (RevCommit ignored : revWalk) {
                        count++;
                    }
                }

                return count;
            }

        } catch (GitAPIException | IOException e) {
            log.error("Failed to count commits for {}/{}: {}", owner, repo, e.getMessage(), e);
            return 0;
        } finally {
            deleteTempDir(tempDir);
        }
    }

    private CommitDTO toCommitDTO(RevCommit commit, boolean includeDescription) {
        PersonIdent authorIdent = commit.getAuthorIdent();
        PersonIdent committerIdent = commit.getCommitterIdent();

        String fullMessage = commit.getFullMessage();
        String[] lines = fullMessage.split("\n", 2);
        String title = lines[0];
        String description = lines.length > 1 ? lines[1].trim() : "";

        List<String> parentShas = new ArrayList<>();
        for (RevCommit parent : commit.getParents()) {
            parentShas.add(parent.getName());
        }

        return CommitDTO.builder()
                .sha(commit.getName())
                .shortSha(commit.getName().substring(0, 7))
                .message(fullMessage)
                .title(title)
                .description(includeDescription ? description : null)
                .author(AuthorDTO.builder()
                        .name(authorIdent.getName())
                        .email(authorIdent.getEmailAddress())
                        .date(authorIdent.getWhen().toInstant())
                        .build())
                .committer(AuthorDTO.builder()
                        .name(committerIdent.getName())
                        .email(committerIdent.getEmailAddress())
                        .date(committerIdent.getWhen().toInstant())
                        .build())
                .authorDate(authorIdent.getWhen().toInstant())
                .committerDate(committerIdent.getWhen().toInstant())
                .parentShas(parentShas)
                .build();
    }

    private List<FileChangeDTO> getFileChanges(Repository repository, RevCommit commit) throws IOException {
        List<FileChangeDTO> files = new ArrayList<>();

        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repository);
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
            diffFormatter.setDetectRenames(true);

            ObjectReader reader = repository.newObjectReader();
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, commit.getTree());

            List<DiffEntry> diffs;
            if (commit.getParentCount() > 0) {
                RevCommit parent = commit.getParent(0);
                try (RevWalk revWalk = new RevWalk(repository)) {
                    parent = revWalk.parseCommit(parent.getId());
                }
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, parent.getTree());
                diffs = diffFormatter.scan(oldTreeIter, newTreeIter);
            } else {
                // First commit - compare with empty tree
                diffs = diffFormatter.scan(new EmptyTreeIterator(), newTreeIter);
            }

            for (DiffEntry diff : diffs) {
                FileChangeDTO fileChange = FileChangeDTO.builder()
                        .filename(diff.getChangeType() == DiffEntry.ChangeType.DELETE ?
                                diff.getOldPath() : diff.getNewPath())
                        .status(mapChangeType(diff.getChangeType()))
                        .previousFilename(diff.getChangeType() == DiffEntry.ChangeType.RENAME ?
                                diff.getOldPath() : null)
                        .build();

                // Calculate additions and deletions
                try {
                    FileHeader fileHeader = diffFormatter.toFileHeader(diff);
                    int additions = 0;
                    int deletions = 0;

                    for (HunkHeader hunk : fileHeader.getHunks()) {
                        EditList edits = hunk.toEditList();
                        for (var edit : edits) {
                            additions += edit.getEndB() - edit.getBeginB();
                            deletions += edit.getEndA() - edit.getBeginA();
                        }
                    }

                    fileChange.setAdditions(additions);
                    fileChange.setDeletions(deletions);

                    // Get patch
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (DiffFormatter patchFormatter = new DiffFormatter(out)) {
                        patchFormatter.setRepository(repository);
                        patchFormatter.format(diff);
                        fileChange.setPatch(out.toString());
                    }

                } catch (Exception e) {
                    log.debug("Could not calculate diff for {}: {}", fileChange.getFilename(), e.getMessage());
                }

                files.add(fileChange);
            }
        }

        return files;
    }

    private String mapChangeType(DiffEntry.ChangeType changeType) {
        return switch (changeType) {
            case ADD -> "added";
            case MODIFY -> "modified";
            case DELETE -> "deleted";
            case RENAME -> "renamed";
            case COPY -> "copied";
        };
    }

    private String remoteUrl(String owner, String repo) {
        return props.getServer().getHttpUrl() + "/git/" + owner + "/" + repo + ".git";
    }

    private void deleteTempDir(Path dir) {
        if (dir == null) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException e) {
            log.warn("Could not delete temp dir {}: {}", dir, e.getMessage());
        }
    }
}
