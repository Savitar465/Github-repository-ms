package com.githubx.githubrepositoryms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "repositories")
@CompoundIndex(name = "owner_name_idx", def = "{'ownerUsername': 1, 'name': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDocument {

    @Id
    private String id;
    private String name;
    private String fullName;
    private String description;
    private String visibility;
    private String ownerId;
    private String ownerUsername;
    private int starsCount;
    private int forksCount;
    private String defaultBranch;
    private String language;
    private boolean hasIssues;
    private String forkedFromId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
