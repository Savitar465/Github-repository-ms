package com.githubx.githubrepositoryms.dao;

import com.githubx.githubrepositoryms.model.StarDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StarDao extends MongoRepository<StarDocument, String> {

    Optional<StarDocument> findByRepositoryIdAndUserId(String repositoryId, String userId);

    boolean existsByRepositoryIdAndUserId(String repositoryId, String userId);

    void deleteByRepositoryIdAndUserId(String repositoryId, String userId);

    long countByRepositoryId(String repositoryId);
}
