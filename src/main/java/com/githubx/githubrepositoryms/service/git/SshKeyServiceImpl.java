package com.githubx.githubrepositoryms.service.git;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SshKeyServiceImpl implements SshKeyService {

    private final GitServerAdminClient adminClient;

    @Override
    public void syncUserKeys(String username, List<String> publicKeys) {
        if (publicKeys.isEmpty()) {
            adminClient.deleteKeys(username);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String key : publicKeys) {
            sb.append(String.format(
                    "command=\"/usr/local/bin/git-auth %s\",no-pty,no-port-forwarding %s\n",
                    username, key.strip()
            ));
        }
        adminClient.syncKeys(username, sb.toString());
    }
}
