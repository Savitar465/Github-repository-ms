package com.githubx.githubrepositoryms.controller.social;

import com.githubx.githubrepositoryms.service.SocialService;
import com.smithy.g.repo.server.social.api.V1Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${openapi.gitHubSocial.base-path:}")
@RequiredArgsConstructor
public class SocialController implements V1Api {

    private final SocialService socialService;

    @Override
    public ResponseEntity<Void> starRepository(String owner, String repo) {
        return socialService.starRepository(owner, repo);
    }

    @Override
    public ResponseEntity<Void> starRepositoryForAuthenticatedUser(String owner, String repo) {
        return socialService.starRepositoryForAuthenticatedUser(owner, repo);
    }

    @Override
    public ResponseEntity<Void> unstarRepository(String owner, String repo) {
        return socialService.unstarRepository(owner, repo);
    }

    @Override
    public ResponseEntity<Void> unstarRepositoryForAuthenticatedUser(String owner, String repo) {
        return socialService.unstarRepositoryForAuthenticatedUser(owner, repo);
    }
}
