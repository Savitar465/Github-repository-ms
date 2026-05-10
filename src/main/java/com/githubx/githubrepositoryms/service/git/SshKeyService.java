package com.githubx.githubrepositoryms.service.git;

import java.util.List;

public interface SshKeyService {

    void syncUserKeys(String username, List<String> publicKeys);
}
