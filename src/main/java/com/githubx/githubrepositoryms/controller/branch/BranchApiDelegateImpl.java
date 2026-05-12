package com.githubx.githubrepositoryms.controller.branch;

import com.githubx.githubrepositoryms.service.BranchService;
import com.smithy.g.repo.server.branch.api.V1ApiDelegate;
import com.smithy.g.repo.server.branch.model.BranchDTO;
import com.smithy.g.repo.server.branch.model.CreateBranchBody;
import com.smithy.g.repo.server.branch.model.ListBranchesBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchApiDelegateImpl implements V1ApiDelegate {

    private final BranchService branchService;

    @Override
    public ResponseEntity<BranchDTO> createBranch(String owner, String repo,
                                                    CreateBranchBody createBranchBody) {
        return branchService.createBranch(owner, repo, createBranchBody);
    }

    @Override
    public ResponseEntity<Void> deleteBranch(String owner, String repo, String branch) {
        return branchService.deleteBranch(owner, repo, branch);
    }

    @Override
    public ResponseEntity<BranchDTO> getBranch(String owner, String repo, String branch) {
        return branchService.getBranch(owner, repo, branch);
    }

    @Override
    public ResponseEntity<ListBranchesBody> listBranches(String owner, String repo) {
        return branchService.listBranches(owner, repo);
    }
}
