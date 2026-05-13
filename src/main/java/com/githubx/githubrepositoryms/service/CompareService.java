package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.service.compare.BranchCompareResponse;
import com.githubx.githubrepositoryms.service.compare.MergeResult;
import org.springframework.http.ResponseEntity;

public interface CompareService {

    ResponseEntity<BranchCompareResponse> compareBranches(String owner, String repo,
                                                           String base, String head);

    ResponseEntity<MergeResult> mergeBranches(String owner, String repo,
                                               String sourceBranch, String targetBranch,
                                               String strategy, String commitMessage,
                                               String authorName, String authorEmail);
}
