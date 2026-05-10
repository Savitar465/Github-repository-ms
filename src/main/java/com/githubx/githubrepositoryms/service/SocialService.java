package com.githubx.githubrepositoryms.service;

import org.springframework.http.ResponseEntity;

public interface SocialService {

    ResponseEntity<Void> starRepository(String owner, String repo);

    ResponseEntity<Void> starRepositoryForAuthenticatedUser(String owner, String repo);

    ResponseEntity<Void> unstarRepository(String owner, String repo);

    ResponseEntity<Void> unstarRepositoryForAuthenticatedUser(String owner, String repo);
}
