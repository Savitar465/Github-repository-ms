package com.githubx.githubrepositoryms.controller.compare;

import com.githubx.githubrepositoryms.service.CompareService;
import com.githubx.githubrepositoryms.service.compare.BranchCompareResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}