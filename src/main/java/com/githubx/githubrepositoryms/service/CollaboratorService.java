package com.githubx.githubrepositoryms.service;

import com.smithy.g.repo.server.collaborator.model.AddCollaboratorBody;
import com.smithy.g.repo.server.collaborator.model.CollaboratorDTO;
import com.smithy.g.repo.server.collaborator.model.ListCollaboratorsBody;
import com.smithy.g.repo.server.collaborator.model.UpdateCollaboratorRoleBody;
import org.springframework.http.ResponseEntity;

public interface CollaboratorService {

    ResponseEntity<Void> addCollaboratorByUsername(String owner, String repo,
                                                   String collaboratorUsername);

    ResponseEntity<CollaboratorDTO> addCollaboratorWithRole(String owner, String repo,
                                                            AddCollaboratorBody body);

    ResponseEntity<CollaboratorDTO> getCollaborator(String owner, String repo,
                                                    String collaboratorUsername);

    ResponseEntity<ListCollaboratorsBody> listCollaborators(String owner, String repo);

    ResponseEntity<Void> removeCollaborator(String owner, String repo,
                                            String collaboratorUsername);

    ResponseEntity<CollaboratorDTO> updateCollaboratorRole(String owner, String repo,
                                                           String collaboratorUsername,
                                                           UpdateCollaboratorRoleBody body);
}
