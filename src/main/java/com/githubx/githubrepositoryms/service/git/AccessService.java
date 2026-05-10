package com.githubx.githubrepositoryms.service.git;

public interface AccessService {

    boolean canRead(String username, String owner, String repoName);

    boolean canWrite(String username, String owner, String repoName);
}
