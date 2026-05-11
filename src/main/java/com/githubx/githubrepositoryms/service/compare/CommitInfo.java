package com.githubx.githubrepositoryms.service.compare;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.Instant;

public record CommitInfo(
        String sha,
        String shortSha,
        String message,
        String author,
        String authorEmail,
        Instant timestamp
) {
    public static CommitInfo from(RevCommit commit) {
        return new CommitInfo(
                commit.getName(),
                commit.getName().substring(0, 7),
                commit.getShortMessage(),
                commit.getAuthorIdent().getName(),
                commit.getAuthorIdent().getEmailAddress(),
                commit.getAuthorIdent().getWhenAsInstant()
        );
    }
}