package com.githubx.githubrepositoryms.controller.internal;

import com.githubx.githubrepositoryms.service.git.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    @Value("${microservice.auth-token}")
    private String authToken;

    private final AccessService accessService;

    @GetMapping("/ssh-access")
    public ResponseEntity<Void> sshAccess(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader("X-Git-User") String username,
            @RequestHeader("X-Git-Repo") String repoPath,
            @RequestHeader("X-Git-Op") String op) {

        // Standalone mode: skip auth when no token is configured (mirrors git-auth behaviour).
        // When a token is configured both sides must match.
        if (!authToken.isBlank()) {
            if (authHeader == null || !authHeader.equals("Bearer " + authToken)) {
                return ResponseEntity.status(403).build();
            }
        }

        // repoPath format from git-auth: "alice/myapp.git"
        String[] parts = repoPath.split("/", 2);
        if (parts.length != 2) {
            return ResponseEntity.status(403).build();
        }
        String owner = parts[0];
        String repoName = parts[1].replaceAll("\\.git$", "");

        boolean allowed = switch (op) {
            case "read" -> accessService.canRead(username, owner, repoName);
            case "write" -> accessService.canWrite(username, owner, repoName);
            default -> false;
        };

        return allowed ? ResponseEntity.ok().build() : ResponseEntity.status(403).build();
    }
}
