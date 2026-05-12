package com.githubx.githubrepositoryms.service;

import com.smithy.g.repo.server.branch.model.BranchDTO;
import com.smithy.g.repo.server.branch.model.CreateBranchBody;
import com.smithy.g.repo.server.branch.model.ListBranchesBody;
import org.springframework.http.ResponseEntity;

public interface BranchService {

    ResponseEntity<BranchDTO> createBranch(String owner, String repo, CreateBranchBody body);

    ResponseEntity<Void> deleteBranch(String owner, String repo, String branch);

    ResponseEntity<BranchDTO> getBranch(String owner, String repo, String branch);

    ResponseEntity<ListBranchesBody> listBranches(String owner, String repo);

    /**
     * Synchronizes branches from the Git server to MongoDB.
     * This method reads all branches from the Git repository and updates
     * the MongoDB collection to match the actual state.
     *
     * @param owner Repository owner username
     * @param repo  Repository name
     */
    void syncBranchesToMongo(String owner, String repo);
}
