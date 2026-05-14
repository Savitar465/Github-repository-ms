package com.githubx.githubrepositoryms.service.git;

import com.githubx.githubrepositoryms.config.GitServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitOpsServiceImpl implements GitOpsService {

    private final GitServerAdminClient adminClient;
    private final GitServerProperties props;

    @Override
    public void createBareRepo(String owner, String name) {
        adminClient.createRepo(owner, name);
    }

    @Override
    public void deleteBareRepo(String owner, String name) {
        adminClient.deleteRepo(owner, name);
    }

    @Override
    public void forkBareRepo(String sourceOwner, String sourceName, String forkOwner, String forkName) {
        adminClient.forkRepo(sourceOwner, sourceName, forkOwner, forkName);
    }

    @Override
    public void initializeRepoWithReadme(String owner, String name, String authorName, String authorEmail) {
        String remoteUrl = props.getServer().getHttpUrl() + "/git/" + owner + "/" + name + ".git";
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("git-init-");

            // Initialize a new git repo with main branch
            Git git = Git.init()
                    .setDirectory(tempDir.toFile())
                    .setInitialBranch("main")
                    .call();

            // Add remote origin
            git.remoteAdd()
                    .setName("origin")
                    .setUri(new URIish(remoteUrl))
                    .call();

            // Create README.md
            String readmeContent = "# " + name + "\n\nA new repository.\n";
            Files.writeString(tempDir.resolve("README.md"), readmeContent);

            // Add and commit
            git.add().addFilepattern(".").call();
            git.commit()
                    .setMessage("Initial commit")
                    .setAuthor(authorName, authorEmail)
                    .call();

            // Push to remote
            RefSpec refSpec = new RefSpec("refs/heads/main:refs/heads/main");
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();

            git.close();
            log.info("Initialized repository {}/{} with README.md", owner, name);

        } catch (GitAPIException | IOException | URISyntaxException e) {
            log.error("Failed to initialize repository {}/{} with README: {}", owner, name, e.getMessage(), e);
        } finally {
            deleteTempDir(tempDir);
        }
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
