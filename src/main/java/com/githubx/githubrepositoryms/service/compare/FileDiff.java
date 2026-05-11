package com.githubx.githubrepositoryms.service.compare;

public record FileDiff(
        String path,
        String oldPath,
        String changeType,
        int additions,
        int deletions,
        String patch
) {}