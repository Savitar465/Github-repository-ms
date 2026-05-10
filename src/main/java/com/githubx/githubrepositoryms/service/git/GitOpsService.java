package com.githubx.githubrepositoryms.service.git;

public interface GitOpsService {

    void createBareRepo(String owner, String name);

    void deleteBareRepo(String owner, String name);

    void forkBareRepo(String sourceOwner, String sourceName, String forkOwner, String forkName);
}
