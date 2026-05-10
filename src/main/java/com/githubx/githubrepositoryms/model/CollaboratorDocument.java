package com.githubx.githubrepositoryms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "collaborators")
@CompoundIndex(name = "repo_collaborator_idx", def = "{'repositoryId': 1, 'username': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorDocument {

    @Id
    private String id;
    private String repositoryId;
    private String userId;
    private String username;
    private String role;
    private String avatarUrl;
    private LocalDateTime addedAt;
}
