package com.githubx.githubrepositoryms.controller.branch;

import com.githubx.githubrepositoryms.service.BranchService;
import com.smithy.g.repo.server.branch.api.V1Api;
import com.smithy.g.repo.server.branch.model.BranchDTO;
import com.smithy.g.repo.server.branch.model.CreateBranchBody;
import com.smithy.g.repo.server.branch.model.ListBranchesBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${openapi.gitHubBranch.base-path:}")
@RequiredArgsConstructor
public class BranchController implements V1Api {

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
