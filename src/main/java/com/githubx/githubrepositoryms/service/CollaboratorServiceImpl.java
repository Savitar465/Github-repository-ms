package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.dao.CollaboratorDao;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.mapper.CollaboratorMapper;
import com.githubx.githubrepositoryms.model.CollaboratorDocument;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.smithy.g.repo.server.collaborator.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollaboratorServiceImpl implements CollaboratorService {

    private final CollaboratorDao collaboratorDao;
    private final RepositoryDao repositoryDao;
    private final CollaboratorMapper collaboratorMapper;

    @Override
    public ResponseEntity<Void> addCollaboratorByUsername(String owner, String repo,
                                                           String collaboratorUsername) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String repoId = repoOpt.get().getId();
        if (collaboratorDao.existsByRepositoryIdAndUsername(repoId, collaboratorUsername)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        collaboratorDao.save(CollaboratorDocument.builder()
                .id(UUID.randomUUID().toString())
                .repositoryId(repoId)
                .userId(UUID.randomUUID().toString())
                .username(collaboratorUsername)
                .role(CollaboratorRole.READ.name())
                .addedAt(LocalDateTime.now())
                .build());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CollaboratorDTO> addCollaboratorWithRole(String owner, String repo,
                                                                    AddCollaboratorBody body) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String repoId = repoOpt.get().getId();
        if (collaboratorDao.existsByRepositoryIdAndUsername(repoId, body.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        CollaboratorDocument doc = CollaboratorDocument.builder()
                .id(UUID.randomUUID().toString())
                .repositoryId(repoId)
                .userId(UUID.randomUUID().toString())
                .username(body.getUsername())
                .role(body.getRole().name())
                .addedAt(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(collaboratorMapper.toDto(collaboratorDao.save(doc)));
    }

    @Override
    public ResponseEntity<CollaboratorDTO> getCollaborator(String owner, String repo,
                                                            String collaboratorUsername) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<CollaboratorDocument> found = collaboratorDao
                .findByRepositoryIdAndUsername(repoOpt.get().getId(), collaboratorUsername);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(collaboratorMapper.toDto(found.get()));
    }

    @Override
    public ResponseEntity<ListCollaboratorsBody> listCollaborators(String owner, String repo) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<CollaboratorDTO> collaborators = collaboratorDao.findByRepositoryId(repoOpt.get().getId())
                .stream()
                .map(collaboratorMapper::toDto)
                .toList();
        return ResponseEntity.ok(new ListCollaboratorsBody(collaborators));
    }

    @Override
    public ResponseEntity<Void> removeCollaborator(String owner, String repo,
                                                    String collaboratorUsername) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String repoId = repoOpt.get().getId();
        if (!collaboratorDao.existsByRepositoryIdAndUsername(repoId, collaboratorUsername)) {
            return ResponseEntity.notFound().build();
        }
        collaboratorDao.deleteByRepositoryIdAndUsername(repoId, collaboratorUsername);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CollaboratorDTO> updateCollaboratorRole(String owner, String repo,
                                                                   String collaboratorUsername,
                                                                   UpdateCollaboratorRoleBody body) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<CollaboratorDocument> found = collaboratorDao
                .findByRepositoryIdAndUsername(repoOpt.get().getId(), collaboratorUsername);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        CollaboratorDocument doc = found.get();
        doc.setRole(body.getRole().name());
        return ResponseEntity.ok(collaboratorMapper.toDto(collaboratorDao.save(doc)));
    }
}
