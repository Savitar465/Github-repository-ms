package com.githubx.githubrepositoryms.controller.social;

import com.githubx.githubrepositoryms.service.SocialService;
import com.smithy.g.repo.server.social.api.V1ApiDelegate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialApiDelegateImpl implements V1ApiDelegate {

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
