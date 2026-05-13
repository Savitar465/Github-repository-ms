package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.dto.CommitDTO;

import java.util.List;

public interface CommitService {

    /**
     * List commits for a repository branch.
     *
     * @param owner      Repository owner username
     * @param repo       Repository name
     * @param branch     Branch name (optional, defaults to default branch)
     * @param path       Filter by file path (optional)
     * @param page       Page number (1-based)
     * @param perPage    Items per page
     * @return List of commits
     */
    List<CommitDTO> listCommits(String owner, String repo, String branch, String path, int page, int perPage);

    /**
     * Get a single commit with full details including file changes.
     *
     * @param owner Repository owner username
     * @param repo  Repository name
     * @param sha   Commit SHA (full or short)
     * @return Commit details with file changes
     */
    CommitDTO getCommit(String owner, String repo, String sha);

    /**
     * Get the total number of commits for pagination.
     *
     * @param owner  Repository owner username
     * @param repo   Repository name
     * @param branch Branch name (optional)
     * @return Total commit count
     */
    int countCommits(String owner, String repo, String branch);
}
