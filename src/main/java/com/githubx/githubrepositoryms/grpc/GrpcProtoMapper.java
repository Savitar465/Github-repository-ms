package com.githubx.githubrepositoryms.grpc;

import com.githubx.grpc.proto.*;
import com.smithy.g.repo.server.branch.model.BranchDTO;
import com.smithy.g.repo.server.collaborator.model.CollaboratorDTO;
import com.smithy.g.repo.server.collaborator.model.CollaboratorRole;
import com.smithy.g.repo.server.repository.model.RepoVisibility;
import com.smithy.g.repo.server.repository.model.RepositoryDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GrpcProtoMapper {

    // ─── RepoVisibility ───────────────────────────────────────

    public com.githubx.grpc.proto.RepoVisibility toProtoVisibility(RepoVisibility v) {
        if (v == null) return com.githubx.grpc.proto.RepoVisibility.REPO_VISIBILITY_UNSPECIFIED;
        return switch (v) {
            case PUBLIC -> com.githubx.grpc.proto.RepoVisibility.REPO_VISIBILITY_PUBLIC;
            case PRIVATE -> com.githubx.grpc.proto.RepoVisibility.REPO_VISIBILITY_PRIVATE;
        };
    }

    public RepoVisibility fromProtoVisibility(com.githubx.grpc.proto.RepoVisibility v) {
        return switch (v) {
            case REPO_VISIBILITY_PRIVATE -> RepoVisibility.PRIVATE;
            default -> RepoVisibility.PUBLIC;
        };
    }

    // ─── CollaboratorRole ─────────────────────────────────────

    public com.githubx.grpc.proto.CollaboratorRole toProtoCollaboratorRole(CollaboratorRole r) {
        if (r == null) return com.githubx.grpc.proto.CollaboratorRole.COLLABORATOR_ROLE_UNSPECIFIED;
        return switch (r) {
            case READ -> com.githubx.grpc.proto.CollaboratorRole.COLLABORATOR_ROLE_READ;
            case WRITE -> com.githubx.grpc.proto.CollaboratorRole.COLLABORATOR_ROLE_WRITE;
            case ADMIN -> com.githubx.grpc.proto.CollaboratorRole.COLLABORATOR_ROLE_ADMIN;
            case MAINTAIN -> com.githubx.grpc.proto.CollaboratorRole.COLLABORATOR_ROLE_OWNER;
        };
    }

    public CollaboratorRole fromProtoCollaboratorRole(com.githubx.grpc.proto.CollaboratorRole r) {
        return switch (r) {
            case COLLABORATOR_ROLE_WRITE -> CollaboratorRole.WRITE;
            case COLLABORATOR_ROLE_ADMIN -> CollaboratorRole.ADMIN;
            case COLLABORATOR_ROLE_OWNER -> CollaboratorRole.MAINTAIN;
            default -> CollaboratorRole.READ;
        };
    }

    // ─── RepositoryDTO ────────────────────────────────────────

    public com.githubx.grpc.proto.RepositoryDTO toProtoRepository(RepositoryDTO dto) {
        return com.githubx.grpc.proto.RepositoryDTO.newBuilder()
                .setId(safe(dto.getId()))
                .setName(safe(dto.getName()))
                .setFullName(safe(dto.getFullName()))
                .setDescription(safe(dto.getDescription()))
                .setVisibility(toProtoVisibility(dto.getVisibility()))
                .setOwnerId(safe(dto.getOwnerId()))
                .setOwnerUsername(safe(dto.getOwnerUsername()))
                .setStarsCount(safeBigDecimal(dto.getStarsCount()))
                .setForksCount(safeBigDecimal(dto.getForksCount()))
                .setDefaultBranch(safe(dto.getDefaultBranch()))
                .setLanguage(safe(dto.getLanguage()))
                .setHasIssues(dto.getHasIssues() != null && dto.getHasIssues())
                .setCreatedAt(safe(dto.getCreatedAt()))
                .setUpdatedAt(safe(dto.getUpdatedAt()))
                .build();
    }

    // ─── BranchDTO ────────────────────────────────────────────

    public com.githubx.grpc.proto.BranchDTO toProtoBranch(BranchDTO dto) {
        return com.githubx.grpc.proto.BranchDTO.newBuilder()
                .setName(safe(dto.getName()))
                .setIsDefault(dto.getIsDefault() != null && dto.getIsDefault())
                .setCommitSha(safe(dto.getCommitSha()))
                .build();
    }

    // ─── CollaboratorDTO ──────────────────────────────────────

    public com.githubx.grpc.proto.CollaboratorDTO toProtoCollaborator(CollaboratorDTO dto) {
        return com.githubx.grpc.proto.CollaboratorDTO.newBuilder()
                .setUserId(safe(dto.getUserId()))
                .setUsername(safe(dto.getUsername()))
                .setRole(toProtoCollaboratorRole(dto.getRole()))
                .setAvatarUrl(safe(dto.getAvatarUrl()))
                .setAddedAt(safe(dto.getAddedAt()))
                .build();
    }

    // ─── PaginationMeta ───────────────────────────────────────

    public com.githubx.grpc.proto.PaginationMeta toProtoPagination(
            com.smithy.g.repo.server.repository.model.PaginationMeta m) {
        if (m == null) return com.githubx.grpc.proto.PaginationMeta.getDefaultInstance();
        return com.githubx.grpc.proto.PaginationMeta.newBuilder()
                .setPage(safeBigDecimal(m.getPage()))
                .setPerPage(safeBigDecimal(m.getPerPage()))
                .setTotal(safeBigDecimal(m.getTotal()))
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────

    private String safe(String s) {
        return s != null ? s : "";
    }

    private int safeBigDecimal(BigDecimal b) {
        return b != null ? b.intValue() : 0;
    }
}
