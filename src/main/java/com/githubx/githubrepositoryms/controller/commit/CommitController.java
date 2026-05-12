package com.githubx.githubrepositoryms.controller.commit;

import com.githubx.githubrepositoryms.dto.CommitDTO;
import com.githubx.githubrepositoryms.service.CommitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/repos/{owner}/{repo}")
@RequiredArgsConstructor
public class CommitController {

    private final CommitService commitService;

    /**
     * List commits for a repository.
     * GET /v1/repos/{owner}/{repo}/commits
     */
    @GetMapping("/commits")
    public ResponseEntity<ListCommitsResponse> listCommits(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String path,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int perPage) {

        List<CommitDTO> commits = commitService.listCommits(owner, repo, branch, path, page, perPage);
        int totalCount = commitService.countCommits(owner, repo, branch);
        int totalPages = (int) Math.ceil((double) totalCount / perPage);

        ListCommitsResponse response = new ListCommitsResponse(
                commits,
                new PaginationInfo(page, perPage, totalCount, totalPages)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single commit with details.
     * GET /v1/repos/{owner}/{repo}/commits/{sha}
     */
    @GetMapping("/commits/{sha}")
    public ResponseEntity<CommitDTO> getCommit(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String sha) {

        CommitDTO commit = commitService.getCommit(owner, repo, sha);

        if (commit == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(commit);
    }

    // Response records
    public record ListCommitsResponse(
            List<CommitDTO> commits,
            PaginationInfo pagination
    ) {}

    public record PaginationInfo(
            int page,
            int perPage,
            int total,
            int totalPages
    ) {}
}
