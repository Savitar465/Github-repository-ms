package com.githubx.githubrepositoryms.dao;

import com.githubx.githubrepositoryms.model.FileEntryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileEntryDao extends MongoRepository<FileEntryDocument, String> {

    Optional<FileEntryDocument> findByRepositoryIdAndPathAndBranch(String repositoryId, String path, String branch);

    List<FileEntryDocument> findByRepositoryIdAndBranch(String repositoryId, String branch);

    void deleteByRepositoryIdAndPathAndBranch(String repositoryId, String path, String branch);
}
