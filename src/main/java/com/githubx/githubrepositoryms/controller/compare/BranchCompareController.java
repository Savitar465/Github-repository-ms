package com.githubx.githubrepositoryms.controller.compare;

import com.githubx.githubrepositoryms.service.CompareService;
import com.githubx.githubrepositoryms.service.compare.BranchCompareResponse;
import com.githubx.githubrepositoryms.service.compare.MergeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repos/{owner}/{repo}")
@RequiredArgsConstructor
public class BranchCompareController {

    private final CompareService compareService;

    /**
     * GET /repos/{owner}/{repo}/compare/{base}...{head}
     * GitHub-style compare URL. Example:
     *   GET /repos/alice/myapp/compare/main...feature-x
     */
    @GetMapping("/compare/{base}...{head}")
    public ResponseEntity<BranchCompareResponse> compareBranches(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String base,
            @PathVariable String head) {
        return compareService.compareBranches(owner, repo, base, head);
    }

    /**
     * GET /repos/{owner}/{repo}/compare?base={base}&head={head}
     * Query-param form, useful when branch names contain slashes (feature/login).
     *   GET /repos/alice/myapp/compare?base=main&head=feature/login
     */
    @GetMapping("/compare")
    public ResponseEntity<BranchCompareResponse> compareBranchesQuery(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam String base,
            @RequestParam String head) {
        return compareService.compareBranches(owner, repo, base, head);
    }

    /**
     * POST /repos/{owner}/{repo}/merge
     * Merge source branch into target branch.
     */
    @PostMapping("/merge")
    public ResponseEntity<MergeResult> mergeBranches(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestBody MergeRequest request) {
        return compareService.mergeBranches(
                owner, repo,
                request.sourceBranch(),
                request.targetBranch(),
                request.strategy(),
                request.commitMessage(),
                request.authorName(),
                request.authorEmail()
        );
    }

    public record MergeRequest(
            String sourceBranch,
            String targetBranch,
            String strategy,
            String commitMessage,
            String authorName,
            String authorEmail
    ) {}
}