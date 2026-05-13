package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.client.OrganizationsApiClient;
import com.githubx.githubrepositoryms.dao.CollaboratorDao;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.mapper.CollaboratorMapper;
import com.githubx.githubrepositoryms.model.CollaboratorDocument;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.smithy.g.repo.server.collaborator.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaboratorServiceImpl implements CollaboratorService {

    private final CollaboratorDao collaboratorDao;
    private final RepositoryDao repositoryDao;
    private final CollaboratorMapper collaboratorMapper;
    private final OrganizationsApiClient organizationsApiClient;

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

        // Get direct collaborators from database
        List<CollaboratorDTO> directCollaborators = collaboratorDao.findByRepositoryId(repoOpt.get().getId())
                .stream()
                .map(collaboratorMapper::toDto)
                .collect(Collectors.toList());

        // Track usernames to avoid duplicates
        Set<String> existingUsernames = directCollaborators.stream()
                .map(CollaboratorDTO::getUsername)
                .collect(Collectors.toSet());

        // Get team members from organizations-ms
        try {
            OrganizationsApiClient.RepoAccessResponse repoAccess = organizationsApiClient.getRepoAccess(owner, repo);

            if (repoAccess != null && repoAccess.getTeams() != null) {
                for (OrganizationsApiClient.TeamAccessDTO team : repoAccess.getTeams()) {
                    if (team.getMembers() != null) {
                        for (OrganizationsApiClient.TeamMemberDTO member : team.getMembers()) {
                            // Skip if already a direct collaborator
                            if (existingUsernames.contains(member.getUsername())) {
                                continue;
                            }
                            existingUsernames.add(member.getUsername());

                            // Map team permission to collaborator role
                            String role = mapTeamPermissionToRole(team.getPermission());

                            CollaboratorDTO teamMemberCollaborator = new CollaboratorDTO()
                                    .userId(member.getUserId())
                                    .username(member.getUsername())
                                    .avatarUrl(member.getAvatarUrl())
                                    .role(CollaboratorRole.fromValue(role))
                                    .addedAt(null); // Team members don't have addedAt

                            directCollaborators.add(teamMemberCollaborator);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch team members from organizations-ms for {}/{}: {}", owner, repo, e.getMessage());
        }

        return ResponseEntity.ok(new ListCollaboratorsBody(directCollaborators));
    }

    private String mapTeamPermissionToRole(String permission) {
        if (permission == null) return "read";
        return switch (permission.toUpperCase()) {
            case "ADMIN" -> "admin";
            case "WRITE" -> "write";
            default -> "read";
        };
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
