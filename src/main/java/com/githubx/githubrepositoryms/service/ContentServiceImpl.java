package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.config.AuthContext;
import com.githubx.githubrepositoryms.config.GitServerProperties;
import com.githubx.githubrepositoryms.dao.FileEntryDao;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.mapper.FileEntryMapper;
import com.githubx.githubrepositoryms.model.FileEntryDocument;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.smithy.g.repo.server.content.model.FileContentDTO;
import com.smithy.g.repo.server.content.model.FileEntryDTO;
import com.smithy.g.repo.server.content.model.FileType;
import com.smithy.g.repo.server.content.model.GetFileContentBody;
import com.smithy.g.repo.server.content.model.GetRepoContentsBody;
import com.smithy.g.repo.server.content.model.UploadFileBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Collection;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentServiceImpl implements ContentService {

    private final AuthContext authContext;
    private final FileEntryDao fileEntryDao;
    private final RepositoryDao repositoryDao;
    private final FileEntryMapper fileEntryMapper;
    private final GitServerProperties props;

    // ── getRepoContents ───────────────────────────────────────────────────────

    @Override
    public ResponseEntity<GetRepoContentsBody> getRepoContents(String owner, String repo,
                                                                String path, String ref) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();

        RepositoryDocument repoDoc = repoOpt.get();
        String targetRef = ref != null ? ref : repoDoc.getDefaultBranch();
        String remoteUrl = remoteUrl(owner, repo);
        Path tempDir = null;

        try {
            // Check if repo is empty (no commits)
            Collection<Ref> remoteRefs = Git.lsRemoteRepository().setRemote(remoteUrl).call();
            if (remoteRefs.isEmpty()) {
                // Return empty list for empty repos
                return ResponseEntity.ok(new GetRepoContentsBody(new ArrayList<>()));
            }

            tempDir = Files.createTempDirectory("git-contents-");

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBare(true)
                    .setCloneAllBranches(true)
                    .call()) {

                Repository repository = git.getRepository();
                ObjectId commitId = resolveRef(repository, targetRef);
                if (commitId == null) return ResponseEntity.notFound().build();

                List<FileEntryDTO> entries = listTree(repository, commitId, path, targetRef, repoDoc.getId());
                return ResponseEntity.ok(new GetRepoContentsBody(entries));
            }

        } catch (GitAPIException | IOException e) {
            log.error("Failed to read contents {}/{}: {}", owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } finally {
            deleteTempDir(tempDir);
        }
    }

    private List<FileEntryDTO> listTree(Repository repository, ObjectId commitId,
                                         String path, String branch, String repoId) throws IOException {
        String normalizedPath = path != null ? path.strip().replaceAll("^/+|/+$", "") : "";
        List<FileEntryDTO> entries = new ArrayList<>();

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevTree tree = revWalk.parseCommit(commitId).getTree();

            if (normalizedPath.isEmpty()) {
                try (TreeWalk tw = new TreeWalk(repository)) {
                    tw.addTree(tree);
                    tw.setRecursive(false);
                    while (tw.next()) {
                        entries.add(toDto(tw, tw.getPathString(), repository, branch, repoId));
                    }
                }
            } else {
                try (TreeWalk tw = TreeWalk.forPath(repository, normalizedPath, tree)) {
                    if (tw == null) return entries;

                    if (tw.getFileMode(0).getObjectType() == Constants.OBJ_TREE) {
                        try (TreeWalk sub = new TreeWalk(repository)) {
                            sub.addTree(tw.getObjectId(0));
                            sub.setRecursive(false);
                            while (sub.next()) {
                                String fullPath = normalizedPath + "/" + sub.getPathString();
                                entries.add(toDto(sub, fullPath, repository, branch, repoId));
                            }
                        }
                    } else {
                        entries.add(toDto(tw, normalizedPath, repository, branch, repoId));
                    }
                }
            }
        }
        return entries;
    }

    private FileEntryDTO toDto(TreeWalk tw, String fullPath,
                                Repository repository, String branch, String repoId) throws IOException {
        boolean isTree = tw.getFileMode(0).getObjectType() == Constants.OBJ_TREE;
        long size = 0;
        if (!isTree) {
            size = repository.open(tw.getObjectId(0)).getSize();
        }
        FileEntryDocument doc = FileEntryDocument.builder()
                .repositoryId(repoId)
                .name(tw.getNameString())
                .path(fullPath)
                .type(isTree ? "directory" : "file")
                .size(size)
                .branch(branch)
                .updatedAt(LocalDateTime.now())
                .build();
        return fileEntryMapper.toDto(doc);
    }

    // ── getFileContent ───────────────────────────────────────────────────────

    @Override
    public ResponseEntity<GetFileContentBody> getFileContent(String owner, String repo,
                                                              String filePath, String ref) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();

        RepositoryDocument repoDoc = repoOpt.get();
        String targetRef = ref != null ? ref : repoDoc.getDefaultBranch();
        String remoteUrl = remoteUrl(owner, repo);
        Path tempDir = null;

        try {
            // Check if repo is empty (no commits)
            Collection<Ref> remoteRefs = Git.lsRemoteRepository().setRemote(remoteUrl).call();
            if (remoteRefs.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            tempDir = Files.createTempDirectory("git-file-content-");

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBare(true)
                    .setCloneAllBranches(true)
                    .call()) {

                Repository repository = git.getRepository();
                ObjectId commitId = resolveRef(repository, targetRef);
                if (commitId == null) return ResponseEntity.notFound().build();

                String normalizedPath = filePath.strip().replaceAll("^/+|/+$", "");

                try (RevWalk revWalk = new RevWalk(repository)) {
                    RevTree tree = revWalk.parseCommit(commitId).getTree();

                    try (TreeWalk tw = TreeWalk.forPath(repository, normalizedPath, tree)) {
                        if (tw == null) return ResponseEntity.notFound().build();

                        // Check if it's a file (not a directory)
                        if (tw.getFileMode(0).getObjectType() == Constants.OBJ_TREE) {
                            return ResponseEntity.badRequest().build();
                        }

                        ObjectId objectId = tw.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);
                        byte[] bytes = loader.getBytes();
                        String contentBase64 = Base64.getEncoder().encodeToString(bytes);
                        String sha = objectId.getName();

                        FileContentDTO fileContent = new FileContentDTO()
                                .name(tw.getNameString())
                                .path(normalizedPath)
                                .sha(sha)
                                .type(FileType.FILE)
                                .size(BigDecimal.valueOf(bytes.length))
                                .encoding("base64")
                                .content(contentBase64);

                        return ResponseEntity.ok(new GetFileContentBody().file(fileContent));
                    }
                }
            }

        } catch (GitAPIException | IOException e) {
            log.error("Failed to read file content {}/{}/{}: {}", owner, repo, filePath, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } finally {
            deleteTempDir(tempDir);
        }
    }

    // ── uploadFile ────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<FileEntryDTO> uploadFile(String owner, String repo,
                                                    String path, UploadFileBody body) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();

        RepositoryDocument repoDoc = repoOpt.get();
        String targetBranch = body.getBranch() != null ? body.getBranch() : repoDoc.getDefaultBranch();
        String remoteUrl = remoteUrl(owner, repo);
        byte[] content = Base64.getDecoder().decode(body.getContent());
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("git-upload-");

            Collection<Ref> remoteRefs = Git.lsRemoteRepository().setRemote(remoteUrl).call();
            boolean repoEmpty = remoteRefs.isEmpty();
            boolean branchExists = remoteRefs.stream()
                    .anyMatch(r -> r.getName().equals("refs/heads/" + targetBranch));

            try (Git git = openForWrite(remoteUrl, targetBranch, tempDir, repoEmpty, branchExists)) {
                Path filePath = tempDir.resolve(path);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, content);

                String authorName = authContext.getUsername();
                String authorEmail = authContext.getEmail() != null ? authContext.getEmail() : authorName + "@local";
                git.add().addFilepattern(".").call();
                git.commit()
                        .setMessage(body.getMessage())
                        .setAuthor(authorName, authorEmail)
                        .call();

                RefSpec refSpec = new RefSpec(
                        "refs/heads/" + targetBranch + ":refs/heads/" + targetBranch);
                git.push().setRemote("origin").setRefSpecs(refSpec).call();
            }

            // sync MongoDB
            Optional<FileEntryDocument> existing = fileEntryDao
                    .findByRepositoryIdAndPathAndBranch(repoDoc.getId(), path, targetBranch);

            FileEntryDocument entry = existing.orElseGet(() -> FileEntryDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .repositoryId(repoDoc.getId())
                    .build());

            entry.setName(Paths.get(path).getFileName().toString());
            entry.setPath(path);
            entry.setType("file");
            entry.setSize(content.length);
            entry.setContent(body.getContent());
            entry.setBranch(targetBranch);
            entry.setUpdatedAt(LocalDateTime.now());

            HttpStatus status = existing.isPresent() ? HttpStatus.OK : HttpStatus.CREATED;
            return ResponseEntity.status(status).body(fileEntryMapper.toDto(fileEntryDao.save(entry)));

        } catch (GitAPIException | IOException | URISyntaxException e) {
            log.error("Failed to upload file to {}/{}: {}", owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } finally {
            deleteTempDir(tempDir);
        }
    }

    // ── deleteFile ────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<Void> deleteFile(String owner, String repo, String path,
                                            String message, String branch) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) return ResponseEntity.notFound().build();

        RepositoryDocument repoDoc = repoOpt.get();
        String targetBranch = branch != null ? branch : repoDoc.getDefaultBranch();
        String remoteUrl = remoteUrl(owner, repo);
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("git-delete-");

            boolean branchExists = Git.lsRemoteRepository()
                    .setRemote(remoteUrl).call().stream()
                    .anyMatch(r -> r.getName().equals("refs/heads/" + targetBranch));

            if (!branchExists) return ResponseEntity.notFound().build();

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBranch("refs/heads/" + targetBranch)
                    .setBranchesToClone(Set.of("refs/heads/" + targetBranch))
                    .call()) {

                Path filePath = tempDir.resolve(path);
                if (!Files.exists(filePath)) return ResponseEntity.notFound().build();

                String authorName = authContext.getUsername();
                String authorEmail = authContext.getEmail() != null ? authContext.getEmail() : authorName + "@local";
                git.rm().addFilepattern(path).call();
                git.commit()
                        .setMessage(message != null ? message : "Delete " + path)
                        .setAuthor(authorName, authorEmail)
                        .call();

                RefSpec refSpec = new RefSpec(
                        "refs/heads/" + targetBranch + ":refs/heads/" + targetBranch);
                git.push().setRemote("origin").setRefSpecs(refSpec).call();
            }

            fileEntryDao.deleteByRepositoryIdAndPathAndBranch(repoDoc.getId(), path, targetBranch);
            return ResponseEntity.noContent().build();

        } catch (GitAPIException | IOException e) {
            log.error("Failed to delete file from {}/{}: {}", owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } finally {
            deleteTempDir(tempDir);
        }
    }

    // ── downloadArchive ───────────────────────────────────────────────────────

    @Override
    public ResponseEntity<Void> downloadArchive(String owner, String repo, String ref) {
        // ResponseEntity<Void> cannot carry a binary body.
        // Change the return type to ResponseEntity<byte[]> in ContentService and the
        // Smithy contract to properly stream the ZIP archive.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Git openForWrite(String remoteUrl, String branch, Path workDir,
                              boolean repoEmpty, boolean branchExists)
            throws GitAPIException, IOException, URISyntaxException {

        if (repoEmpty) {
            Git git = Git.init()
                    .setDirectory(workDir.toFile())
                    .setInitialBranch(branch)
                    .call();
            git.remoteAdd().setName("origin").setUri(new URIish(remoteUrl)).call();
            return git;
        }

        if (branchExists) {
            return Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(workDir.toFile())
                    .setBranch("refs/heads/" + branch)
                    .setBranchesToClone(Set.of("refs/heads/" + branch))
                    .call();
        }

        // Branch doesn't exist yet — clone default branch and create it
        Git git = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(workDir.toFile())
                .call();

        git.checkout().setCreateBranch(true).setName(branch).call();
        return git;
    }

    private ObjectId resolveRef(Repository repository, String ref) throws IOException {
        ObjectId id = repository.resolve(ref);
        if (id != null) return id;

        id = repository.resolve("refs/heads/" + ref);
        if (id != null) return id;

        id = repository.resolve("refs/tags/" + ref);
        return id;
    }

    private String remoteUrl(String owner, String repo) {
        return props.getServer().getHttpUrl() + "/git/" + owner + "/" + repo + ".git";
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
