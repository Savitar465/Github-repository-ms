package com.githubx.githubrepositoryms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

@Document(collection = "branches")
@CompoundIndex(name = "repo_branch_idx", def = "{'repositoryId': 1, 'name': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDocument {

    @Id
    private String id;
    private String repositoryId;
    private String name;
    private Boolean isDefault;
    private String commitSha;
}
