package com.githubx.githubrepositoryms.dao;

import com.githubx.githubrepositoryms.model.RepositoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryDao extends MongoRepository<RepositoryDocument, String> {

    Optional<RepositoryDocument> findByOwnerUsernameAndName(String ownerUsername, String name);

    List<RepositoryDocument> findByOwnerUsername(String ownerUsername);

    List<RepositoryDocument> findByOwnerUsernameAndVisibility(String ownerUsername, String visibility);

    List<RepositoryDocument> findByForkedFromId(String forkedFromId);

    boolean existsByOwnerUsernameAndName(String ownerUsername, String name);
}
