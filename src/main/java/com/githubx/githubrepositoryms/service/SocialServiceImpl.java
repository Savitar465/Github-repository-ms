package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.config.AuthContext;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.dao.StarDao;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.githubx.githubrepositoryms.model.StarDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private final AuthContext authContext;
    private final StarDao starDao;
    private final RepositoryDao repositoryDao;

    @Override
    public ResponseEntity<Void> starRepository(String owner, String repo) {
        return star(owner, repo, authContext.getUserId());
    }

    @Override
    public ResponseEntity<Void> starRepositoryForAuthenticatedUser(String owner, String repo) {
        return star(owner, repo, authContext.getUserId());
    }

    @Override
    public ResponseEntity<Void> unstarRepository(String owner, String repo) {
        return unstar(owner, repo, authContext.getUserId());
    }

    @Override
    public ResponseEntity<Void> unstarRepositoryForAuthenticatedUser(String owner, String repo) {
        return unstar(owner, repo, authContext.getUserId());
    }

    private ResponseEntity<Void> star(String owner, String repo, String userId) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RepositoryDocument repoDoc = repoOpt.get();
        if (!starDao.existsByRepositoryIdAndUserId(repoDoc.getId(), userId)) {
            starDao.save(StarDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .repositoryId(repoDoc.getId())
                    .userId(userId)
                    .starredAt(LocalDateTime.now())
                    .build());
            repoDoc.setStarsCount(repoDoc.getStarsCount() + 1);
            repositoryDao.save(repoDoc);
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Void> unstar(String owner, String repo, String userId) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RepositoryDocument repoDoc = repoOpt.get();
        if (starDao.existsByRepositoryIdAndUserId(repoDoc.getId(), userId)) {
            starDao.deleteByRepositoryIdAndUserId(repoDoc.getId(), userId);
            repoDoc.setStarsCount(Math.max(0, repoDoc.getStarsCount() - 1));
            repositoryDao.save(repoDoc);
        }
        return ResponseEntity.noContent().build();
    }
}
