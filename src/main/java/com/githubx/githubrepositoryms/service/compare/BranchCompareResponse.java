package com.githubx.githubrepositoryms.service.compare;

import java.util.List;

public record BranchCompareResponse(
        String baseBranch,
        String headBranch,
        int totalCommits,
        int filesChanged,
        int additions,
        int deletions,
        List<CommitInfo> commits,
        List<FileDiff> files
) {}