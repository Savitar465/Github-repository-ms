package com.githubx.githubrepositoryms.dao;

import com.githubx.githubrepositoryms.model.BranchDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchDao extends MongoRepository<BranchDocument, String> {

    Optional<BranchDocument> findByRepositoryIdAndName(String repositoryId, String name);

    List<BranchDocument> findByRepositoryId(String repositoryId);

    boolean existsByRepositoryIdAndIsDefault(String repositoryId, boolean isDefault);

    void deleteByRepositoryIdAndName(String repositoryId, String name);
}
