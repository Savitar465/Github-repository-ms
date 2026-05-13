package com.githubx.githubrepositoryms.service.compare;

public record MergeResult(
    boolean success,
    String message,
    String mergeCommitSha,
    String strategy
) {
    public static MergeResult success(String commitSha, String strategy) {
        return new MergeResult(true, "Merge successful", commitSha, strategy);
    }

    public static MergeResult failure(String message) {
        return new MergeResult(false, message, null, null);
    }
}
