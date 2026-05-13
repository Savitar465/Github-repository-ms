package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.config.GitServerProperties;
import com.githubx.githubrepositoryms.dao.BranchDao;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.mapper.BranchMapper;
import com.githubx.githubrepositoryms.model.BranchDocument;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.smithy.g.repo.server.branch.model.BranchDTO;
import com.smithy.g.repo.server.branch.model.CreateBranchBody;
import com.smithy.g.repo.server.branch.model.ListBranchesBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchServiceImpl implements BranchService {

    private final BranchDao branchDao;
    private final RepositoryDao repositoryDao;
    private final BranchMapper branchMapper;
    private final GitServerProperties props;

    @Override
    public ResponseEntity<BranchDTO> createBranch(String owner, String repo, CreateBranchBody body) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepositoryDocument repoDoc = repoOpt.get();
        String repoId = repoDoc.getId();
        String newBranchName = body.getName();
        String sourceBranch = body.getFromBranch() != null ? body.getFromBranch() : repoDoc.getDefaultBranch();

        // Check if branch already exists in Git
        String remoteUrl = remoteUrl(owner, repo);
        try {
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(remoteUrl)
                    .setHeads(true)
                    .call();

            boolean branchExists = refs.stream()
                    .anyMatch(r -> r.getName().equals("refs/heads/" + newBranchName));

            if (branchExists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Find source branch commit
            String sourceCommitSha = null;
            for (Ref ref : refs) {
                String branchName = ref.getName().replace("refs/heads/", "");
                if (branchName.equals(sourceBranch)) {
                    sourceCommitSha = ref.getObjectId() != null ? ref.getObjectId().getName() : null;
                    break;
                }
            }

            if (sourceCommitSha == null) {
                log.warn("Source branch {} not found for creating new branch {}", sourceBranch, newBranchName);
                return ResponseEntity.badRequest().build();
            }

            // Create branch using JGit
            Path tempDir = null;
            try {
                tempDir = Files.createTempDirectory("git-create-branch-");

                try (Git git = Git.cloneRepository()
                        .setURI(remoteUrl)
                        .setDirectory(tempDir.toFile())
                        .setBranch("refs/heads/" + sourceBranch)
                        .setBranchesToClone(Set.of("refs/heads/" + sourceBranch))
                        .call()) {

                    // Create new branch from source
                    Ref newBranchRef = git.branchCreate()
                            .setName(newBranchName)
                            .setStartPoint(sourceBranch)
                            .call();

                    // Push the new branch to remote
                    RefSpec refSpec = new RefSpec("refs/heads/" + newBranchName + ":refs/heads/" + newBranchName);
                    git.push()
                            .setRemote("origin")
                            .setRefSpecs(refSpec)
                            .call();

                    String newCommitSha = newBranchRef.getObjectId() != null
                            ? newBranchRef.getObjectId().getName()
                            : sourceCommitSha;

                    // Save to MongoDB
                    BranchDocument branch = BranchDocument.builder()
                            .id(UUID.randomUUID().toString())
                            .repositoryId(repoId)
                            .name(newBranchName)
                            .isDefault(Boolean.FALSE)
                            .commitSha(newCommitSha)
                            .build();

                    log.info("Created branch {} from {} in {}/{}", newBranchName, sourceBranch, owner, repo);
                    return ResponseEntity.status(HttpStatus.CREATED).body(branchMapper.toDto(branchDao.save(branch)));
                }
            } finally {
                deleteTempDir(tempDir);
            }

        } catch (GitAPIException | IOException e) {
            log.error("Failed to create branch {} in {}/{}: {}", newBranchName, owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteBranch(String owner, String repo, String branch) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepositoryDocument repoDoc = repoOpt.get();
        String repoId = repoDoc.getId();

        // Cannot delete the default branch
        if (branch.equals(repoDoc.getDefaultBranch())) {
            log.warn("Cannot delete default branch {} in {}/{}", branch, owner, repo);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String remoteUrl = remoteUrl(owner, repo);
        Path tempDir = null;

        try {
            // Check if branch exists in Git
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(remoteUrl)
                    .setHeads(true)
                    .call();

            boolean branchExists = refs.stream()
                    .anyMatch(r -> r.getName().equals("refs/heads/" + branch));

            if (!branchExists) {
                // Branch doesn't exist in Git, just clean MongoDB
                branchDao.deleteByRepositoryIdAndName(repoId, branch);
                return ResponseEntity.notFound().build();
            }

            // Delete branch from Git using push --delete
            tempDir = Files.createTempDirectory("git-delete-branch-");

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(tempDir.toFile())
                    .setBare(true)
                    .setCloneAllBranches(true)
                    .call()) {

                // Push delete refspec to remove the branch
                RefSpec deleteRefSpec = new RefSpec(":refs/heads/" + branch);
                git.push()
                        .setRemote("origin")
                        .setRefSpecs(deleteRefSpec)
                        .call();

                log.info("Deleted branch {} from {}/{}", branch, owner, repo);
            }

            // Remove from MongoDB
            branchDao.deleteByRepositoryIdAndName(repoId, branch);
            return ResponseEntity.noContent().build();

        } catch (GitAPIException | IOException e) {
            log.error("Failed to delete branch {} from {}/{}: {}", branch, owner, repo, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } finally {
            deleteTempDir(tempDir);
        }
    }

    @Override
    public ResponseEntity<BranchDTO> getBranch(String owner, String repo, String branch) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepositoryDocument repoDoc = repoOpt.get();
        String remoteUrl = remoteUrl(owner, repo);

        try {
            // Query git server directly for the branch
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(remoteUrl)
                    .setHeads(true)
                    .call();

            for (Ref ref : refs) {
                String branchName = ref.getName().replace("refs/heads/", "");
                if (branchName.equals(branch)) {
                    boolean isDefault = branchName.equals(repoDoc.getDefaultBranch());
                    String sha = ref.getObjectId() != null ? ref.getObjectId().getName() : "";
                    return ResponseEntity.ok(new BranchDTO(branchName, isDefault, sha));
                }
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.warn("Could not get branch from git server for {}/{}/{}: {}, falling back to MongoDB",
                     owner, repo, branch, e.getMessage());
            // Fallback to MongoDB
            Optional<BranchDocument> found = branchDao.findByRepositoryIdAndName(repoDoc.getId(), branch);
            if (found.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(branchMapper.toDto(found.get()));
        }
    }

    @Override
    public ResponseEntity<ListBranchesBody> listBranches(String owner, String repo) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RepositoryDocument repoDoc = repoOpt.get();
        String url = remoteUrl(owner, repo);

        try {
            // Query git server directly for branches
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(url)
                    .setHeads(true) // Only get branches (refs/heads/*)
                    .call();

            List<BranchDTO> branches = new ArrayList<>();
            for (Ref ref : refs) {
                String branchName = ref.getName().replace("refs/heads/", "");
                boolean isDefault = branchName.equals(repoDoc.getDefaultBranch());
                String sha = ref.getObjectId() != null ? ref.getObjectId().getName() : "";

                BranchDTO dto = new BranchDTO(branchName, isDefault, sha);
                branches.add(dto);
            }

            log.debug("Found {} branches for {}/{} from git server", branches.size(), owner, repo);
            return ResponseEntity.ok(new ListBranchesBody(branches));

        } catch (Exception e) {
            log.warn("Could not list branches from git server for {}/{}: {}, falling back to MongoDB",
                     owner, repo, e.getMessage());
            // Fallback to MongoDB if git server is unreachable
            List<BranchDTO> branches = branchDao.findByRepositoryId(repoDoc.getId())
                    .stream()
                    .map(branchMapper::toDto)
                    .toList();
            return ResponseEntity.ok(new ListBranchesBody(branches));
        }
    }

    private String remoteUrl(String owner, String repo) {
        return props.getServer().getHttpUrl() + "/git/" + owner + "/" + repo + ".git";
    }

    /**
     * Synchronizes branches from Git server to MongoDB.
     * This ensures MongoDB always reflects the actual state of the Git repository.
     */
    @Override
    public void syncBranchesToMongo(String owner, String repo) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            log.warn("Repository not found for sync: {}/{}", owner, repo);
            return;
        }

        RepositoryDocument repoDoc = repoOpt.get();
        String repoId = repoDoc.getId();
        String remoteUrl = remoteUrl(owner, repo);

        try {
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(remoteUrl)
                    .setHeads(true)
                    .call();

            // Get current branches from MongoDB
            List<BranchDocument> mongoBranches = branchDao.findByRepositoryId(repoId);
            Set<String> mongoBranchNames = mongoBranches.stream()
                    .map(BranchDocument::getName)
                    .collect(java.util.stream.Collectors.toSet());

            // Sync from Git to MongoDB
            Set<String> gitBranchNames = new java.util.HashSet<>();
            for (Ref ref : refs) {
                String branchName = ref.getName().replace("refs/heads/", "");
                gitBranchNames.add(branchName);

                String sha = ref.getObjectId() != null ? ref.getObjectId().getName() : "";
                boolean isDefault = branchName.equals(repoDoc.getDefaultBranch());

                Optional<BranchDocument> existing = branchDao.findByRepositoryIdAndName(repoId, branchName);
                if (existing.isPresent()) {
                    // Update existing branch
                    BranchDocument doc = existing.get();
                    if (!sha.equals(doc.getCommitSha()) || !Boolean.valueOf(isDefault).equals(doc.getIsDefault())) {
                        doc.setCommitSha(sha);
                        doc.setIsDefault(isDefault);
                        branchDao.save(doc);
                        log.debug("Updated branch {} in MongoDB", branchName);
                    }
                } else {
                    // Create new branch in MongoDB
                    BranchDocument newBranch = BranchDocument.builder()
                            .id(UUID.randomUUID().toString())
                            .repositoryId(repoId)
                            .name(branchName)
                            .isDefault(isDefault)
                            .commitSha(sha)
                            .build();
                    branchDao.save(newBranch);
                    log.debug("Created branch {} in MongoDB", branchName);
                }
            }

            // Remove branches that no longer exist in Git
            for (BranchDocument mongoBranch : mongoBranches) {
                if (!gitBranchNames.contains(mongoBranch.getName())) {
                    branchDao.delete(mongoBranch);
                    log.debug("Removed stale branch {} from MongoDB", mongoBranch.getName());
                }
            }

            log.info("Synchronized {} branches for {}/{}", gitBranchNames.size(), owner, repo);

        } catch (GitAPIException e) {
            log.error("Failed to sync branches for {}/{}: {}", owner, repo, e.getMessage(), e);
        }
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
