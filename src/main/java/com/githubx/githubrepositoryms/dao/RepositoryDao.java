package com.githubx.githubrepositoryms.dao;

import com.githubx.githubrepositoryms.model.RepositoryDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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

    @Query("{ 'visibility': 'PUBLIC', $or: [ " +
           "{ 'name': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } }, " +
           "{ 'fullName': { $regex: ?0, $options: 'i' } } ] }")
    Page<RepositoryDocument> searchPublicRepositories(String query, Pageable pageable);

    @Query("{ 'visibility': 'PUBLIC' }")
    Page<RepositoryDocument> findAllPublicRepositories(Pageable pageable);
}
