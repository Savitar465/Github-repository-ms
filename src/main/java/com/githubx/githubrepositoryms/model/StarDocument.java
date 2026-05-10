package com.githubx.githubrepositoryms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "stars")
@CompoundIndex(name = "repo_user_star_idx", def = "{'repositoryId': 1, 'userId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarDocument {

    @Id
    private String id;
    private String repositoryId;
    private String userId;
    private LocalDateTime starredAt;
}
