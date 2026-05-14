package com.githubx.githubrepositoryms.service.git;

public interface GitOpsService {

    void createBareRepo(String owner, String name);

    void deleteBareRepo(String owner, String name);

    void forkBareRepo(String sourceOwner, String sourceName, String forkOwner, String forkName);

    /**
     * Initialize a repository with an initial commit containing a README.md file.
     * This creates the main branch so it's visible immediately after repo creation.
     */
    void initializeRepoWithReadme(String owner, String name, String authorName, String authorEmail);
}
