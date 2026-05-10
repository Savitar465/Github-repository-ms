package com.githubx.githubrepositoryms.dao;

import com.githubx.githubrepositoryms.model.CollaboratorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaboratorDao extends MongoRepository<CollaboratorDocument, String> {

    Optional<CollaboratorDocument> findByRepositoryIdAndUsername(String repositoryId, String username);

    List<CollaboratorDocument> findByRepositoryId(String repositoryId);

    boolean existsByRepositoryIdAndUsername(String repositoryId, String username);

    void deleteByRepositoryIdAndUsername(String repositoryId, String username);
}
