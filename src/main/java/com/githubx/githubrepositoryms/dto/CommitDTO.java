package com.githubx.githubrepositoryms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitDTO {
    private String sha;
    private String shortSha;
    private String message;
    private String title;
    private String description;
    private AuthorDTO author;
    private AuthorDTO committer;
    private Instant authorDate;
    private Instant committerDate;
    private List<String> parentShas;
    private List<FileChangeDTO> files;
    private int additions;
    private int deletions;
    private int filesChanged;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDTO {
        private String name;
        private String email;
        private Instant date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileChangeDTO {
        private String filename;
        private String status; // added, modified, deleted, renamed
        private int additions;
        private int deletions;
        private String patch;
        private String previousFilename;
    }
}
