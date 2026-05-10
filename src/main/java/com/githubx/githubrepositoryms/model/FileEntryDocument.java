package com.githubx.githubrepositoryms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "file_entries")
@CompoundIndex(name = "repo_path_branch_idx", def = "{'repositoryId': 1, 'path': 1, 'branch': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntryDocument {

    @Id
    private String id;
    private String repositoryId;
    private String name;
    private String path;
    private String type;
    private long size;
    private String contentType;
    private String downloadUrl;
    private String branch;
    private String content;
    private LocalDateTime updatedAt;
}
