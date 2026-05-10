package com.githubx.githubrepositoryms.controller.collaborator;

import com.githubx.githubrepositoryms.service.CollaboratorService;
import com.smithy.g.repo.server.collaborator.api.V1Api;
import com.smithy.g.repo.server.collaborator.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${openapi.gitHubCollaborator.base-path:}")
@RequiredArgsConstructor
public class CollaboratorController implements V1Api {

    private final CollaboratorService collaboratorService;

    @Override
    public ResponseEntity<Void> addCollaboratorByUsername(String owner, String repo,
                                                           String collaboratorUsername) {
        return collaboratorService.addCollaboratorByUsername(owner, repo, collaboratorUsername);
    }

    @Override
    public ResponseEntity<CollaboratorDTO> addCollaboratorWithRole(String owner, String repo,
                                                                    AddCollaboratorBody addCollaboratorBody) {
        return collaboratorService.addCollaboratorWithRole(owner, repo, addCollaboratorBody);
    }

    @Override
    public ResponseEntity<CollaboratorDTO> getCollaborator(String owner, String repo,
                                                            String collaboratorUsername) {
        return collaboratorService.getCollaborator(owner, repo, collaboratorUsername);
    }

    @Override
    public ResponseEntity<ListCollaboratorsBody> listCollaborators(String owner, String repo) {
        return collaboratorService.listCollaborators(owner, repo);
    }

    @Override
    public ResponseEntity<Void> removeCollaborator(String owner, String repo,
                                                    String collaboratorUsername) {
        return collaboratorService.removeCollaborator(owner, repo, collaboratorUsername);
    }

    @Override
    public ResponseEntity<CollaboratorDTO> updateCollaboratorRole(String owner, String repo,
                                                                   String collaboratorUsername,
                                                                   UpdateCollaboratorRoleBody updateCollaboratorRoleBody) {
        return collaboratorService.updateCollaboratorRole(owner, repo, collaboratorUsername,
                updateCollaboratorRoleBody);
    }
}
