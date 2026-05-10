package com.githubx.githubrepositoryms.service.git;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitOpsServiceImpl implements GitOpsService {

    private final GitServerAdminClient adminClient;

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
}
