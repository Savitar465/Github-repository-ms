package com.githubx.githubrepositoryms.controller.branch;

import com.githubx.githubrepositoryms.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BranchSyncController {

    private final BranchService branchService;

    /**
     * Synchronizes branches from Git server to MongoDB.
     * This endpoint can be called to manually trigger synchronization.
     */
    @PostMapping("/v1/repos/{owner}/{repo}/branches/sync")
    public ResponseEntity<Void> syncBranches(@PathVariable String owner, @PathVariable String repo) {
        branchService.syncBranchesToMongo(owner, repo);
        return ResponseEntity.ok().build();
    }
}
