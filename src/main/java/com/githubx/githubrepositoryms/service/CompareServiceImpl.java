package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.config.GitServerProperties;
import com.githubx.githubrepositoryms.service.compare.BranchCompareResponse;
import com.githubx.githubrepositoryms.service.compare.CommitInfo;
import com.githubx.githubrepositoryms.service.compare.FileDiff;
import com.githubx.githubrepositoryms.service.compare.MergeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompareServiceImpl implements CompareService {

    private final GitServerProperties props;

    @Value("${microservice.auth-token:}")
    private String gitAuthToken;

    @Override
    public ResponseEntity<BranchCompareResponse> compareBranches(String owner, String repo,
                                                                   String base, String head) {
        String remoteUrl = props.getServer().getHttpUrl() + "/git/" + owner + "/" + repo + ".git";
        Path tempDir = null;

        try {
            Set<PosixFilePermission> perms = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
            );
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            tempDir = Files.createTempDirectory("git-compare-", attr);

            log.debug("Cloning {} into {}", remoteUrl, tempDir);

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBare(true)
                    .setCloneAllBranches(true)
                    .call()) {

                Repository repository = git.getRepository();

                ObjectId baseId = resolveRef(repository, base);
                ObjectId headId = resolveRef(repository, head);

                List<CommitInfo> commits = getCommitsBetween(repository, baseId, headId);
                List<FileDiff> files = getFileDiffs(repository, baseId, headId);

                int totalAdditions = files.stream().mapToInt(FileDiff::additions).sum();
                int totalDeletions = files.stream().mapToInt(FileDiff::deletions).sum();

                return ResponseEntity.ok(new BranchCompareResponse(
                        base, head,
                        commits.size(),
                        files.size(),
                        totalAdditions,
                        totalDeletions,
                        commits,
                        files
                ));
            }

        } catch (IllegalArgumentException e) {
            log.warn("Branch not found in {}/{}: {}", owner, repo, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (GitAPIException e) {
            log.error("Failed to clone repository {}/{}: {}", owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (IOException e) {
            log.error("IO error comparing {}/{}: {}", owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } finally {
            deleteTempDir(tempDir);
        }
    }

    @Override
    public ResponseEntity<MergeResult> mergeBranches(String owner, String repo,
                                                      String sourceBranch, String targetBranch,
                                                      String strategy, String commitMessage,
                                                      String authorName, String authorEmail) {
        String remoteUrl = props.getServer().getHttpUrl() + "/git/" + owner + "/" + repo + ".git";
        Path tempDir = null;

        try {
            Set<PosixFilePermission> perms = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
            );
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            tempDir = Files.createTempDirectory("git-merge-", attr);

            log.info("Cloning {} into {} for merge operation", remoteUrl, tempDir);

            // Clone the repository (non-bare for merge operations)
            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBare(false)
                    .setCloneAllBranches(true)
                    .call()) {

                // Checkout target branch
                git.checkout()
                        .setName(targetBranch)
                        .setCreateBranch(false)
                        .call();

                log.info("Checked out target branch: {}", targetBranch);

                // Get source branch ref
                ObjectId sourceId = git.getRepository().resolve("origin/" + sourceBranch);
                if (sourceId == null) {
                    sourceId = git.getRepository().resolve(sourceBranch);
                }
                if (sourceId == null) {
                    return ResponseEntity.badRequest()
                            .body(MergeResult.failure("Source branch not found: " + sourceBranch));
                }

                // Configure merge
                MergeCommand mergeCommand = git.merge()
                        .include(sourceId)
                        .setMessage(commitMessage != null ? commitMessage :
                                "Merge branch '" + sourceBranch + "' into " + targetBranch);

                // Set merge strategy
                if ("squash".equalsIgnoreCase(strategy)) {
                    mergeCommand.setSquash(true);
                } else if ("rebase".equalsIgnoreCase(strategy)) {
                    // For rebase, we'd need a different approach
                    // For now, default to merge
                    mergeCommand.setFastForward(MergeCommand.FastForwardMode.NO_FF);
                } else {
                    // Default merge commit
                    mergeCommand.setFastForward(MergeCommand.FastForwardMode.NO_FF);
                }

                // Execute merge
                org.eclipse.jgit.api.MergeResult mergeResult = mergeCommand.call();

                if (!mergeResult.getMergeStatus().isSuccessful()) {
                    log.warn("Merge failed with status: {}", mergeResult.getMergeStatus());
                    return ResponseEntity.unprocessableEntity()
                            .body(MergeResult.failure("Merge failed: " + mergeResult.getMergeStatus().name()));
                }

                // If squash, we need to commit
                String finalCommitSha;
                if ("squash".equalsIgnoreCase(strategy)) {
                    PersonIdent author = new PersonIdent(
                            authorName != null ? authorName : "GitHub Clone",
                            authorEmail != null ? authorEmail : "noreply@githubclone.local"
                    );
                    RevCommit squashCommit = git.commit()
                            .setMessage(commitMessage != null ? commitMessage :
                                    "Squash merge branch '" + sourceBranch + "' into " + targetBranch)
                            .setAuthor(author)
                            .setCommitter(author)
                            .call();
                    finalCommitSha = squashCommit.getName();
                } else {
                    finalCommitSha = mergeResult.getNewHead() != null ?
                            mergeResult.getNewHead().getName() : null;
                }

                log.info("Merge successful, new commit: {}", finalCommitSha);

                // Push the result back to origin
                git.push()
                        .setRemote("origin")
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider("git", gitAuthToken))
                        .call();

                log.info("Pushed merge result to origin/{}", targetBranch);

                return ResponseEntity.ok(MergeResult.success(finalCommitSha, strategy != null ? strategy : "merge"));
            }

        } catch (GitAPIException e) {
            log.error("Git error during merge {}/{}: {}", owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(MergeResult.failure("Git error: " + e.getMessage()));
        } catch (IOException e) {
            log.error("IO error during merge {}/{}: {}", owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(MergeResult.failure("IO error: " + e.getMessage()));
        } finally {
            deleteTempDir(tempDir);
        }
    }

    private ObjectId resolveRef(Repository repository, String ref) throws IOException {
        ObjectId id = repository.resolve(ref);
        if (id != null) return id;

        id = repository.resolve("refs/heads/" + ref);
        if (id != null) return id;

        id = repository.resolve("refs/tags/" + ref);
        if (id != null) return id;

        throw new IllegalArgumentException("Branch or ref not found: " + ref);
    }

    private List<CommitInfo> getCommitsBetween(Repository repository,
                                                ObjectId baseId, ObjectId headId) throws IOException {
        List<CommitInfo> commits = new ArrayList<>();
        try (RevWalk revWalk = new RevWalk(repository)) {
            revWalk.markStart(revWalk.parseCommit(headId));
            revWalk.markUninteresting(revWalk.parseCommit(baseId));
            for (RevCommit commit : revWalk) {
                commits.add(CommitInfo.from(commit));
            }
        }
        return commits;
    }

    private List<FileDiff> getFileDiffs(Repository repository,
                                         ObjectId baseId, ObjectId headId) throws IOException {
        List<FileDiff> result = new ArrayList<>();

        try (ObjectReader reader = repository.newObjectReader();
             RevWalk revWalk = new RevWalk(repository);
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             DiffFormatter formatter = new DiffFormatter(out)) {

            formatter.setRepository(repository);
            formatter.setDiffComparator(RawTextComparator.DEFAULT);
            formatter.setDetectRenames(true);

            AbstractTreeIterator baseTree = treeParser(reader, revWalk, baseId);
            AbstractTreeIterator headTree = treeParser(reader, revWalk, headId);

            for (DiffEntry entry : formatter.scan(baseTree, headTree)) {
                out.reset();
                formatter.format(entry);
                formatter.flush();
                String patch = out.toString();

                result.add(new FileDiff(
                        effectivePath(entry),
                        entry.getOldPath(),
                        entry.getChangeType().name(),
                        countPatchLines(patch, '+'),
                        countPatchLines(patch, '-'),
                        patch
                ));
            }
        }
        return result;
    }

    private AbstractTreeIterator treeParser(ObjectReader reader, RevWalk revWalk,
                                             ObjectId commitId) throws IOException {
        RevCommit commit = revWalk.parseCommit(commitId);
        CanonicalTreeParser parser = new CanonicalTreeParser();
        parser.reset(reader, commit.getTree().getId());
        return parser;
    }

    private String effectivePath(DiffEntry entry) {
        return entry.getChangeType() == DiffEntry.ChangeType.DELETE
                ? entry.getOldPath()
                : entry.getNewPath();
    }

    private int countPatchLines(String patch, char prefix) {
        String triplePrefix = String.valueOf(prefix).repeat(3);
        int count = 0;
        for (String line : patch.split("\n")) {
            if (!line.isEmpty() && line.charAt(0) == prefix && !line.startsWith(triplePrefix)) {
                count++;
            }
        }
        return count;
    }

    private void deleteTempDir(Path dir) {
        if (dir == null) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.delete(p); } catch (IOException ignored) {}
                });
        } catch (IOException e) {
            log.warn("Could not delete temp dir {}: {}", dir, e.getMessage());
        }
    }
}
