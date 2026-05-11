package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.service.compare.BranchCompareResponse;
import org.springframework.http.ResponseEntity;

public interface CompareService {

    ResponseEntity<BranchCompareResponse> compareBranches(String owner, String repo,
                                                           String base, String head);
}
