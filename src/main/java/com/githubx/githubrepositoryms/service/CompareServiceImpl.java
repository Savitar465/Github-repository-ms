package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.config.GitServerProperties;
import com.githubx.githubrepositoryms.service.compare.BranchCompareResponse;
import com.githubx.githubrepositoryms.service.compare.CommitInfo;
import com.githubx.githubrepositoryms.service.compare.FileDiff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompareServiceImpl implements CompareService {

    private final GitServerProperties props;

    @Override
    public ResponseEntity<BranchCompareResponse> compareBranches(String owner, String repo,
                                                                   String base, String head) {
        String remoteUrl = props.getServer().getHttpUrl() + "/git/" + owner + "/" + repo + ".git";
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("git-compare-");

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
