package com.githubx.githubrepositoryms.grpc;

import com.githubx.grpc.proto.*;
import com.githubx.githubrepositoryms.service.BranchService;
import com.githubx.githubrepositoryms.service.CollaboratorService;
import com.githubx.githubrepositoryms.service.RepositoryService;
import com.githubx.githubrepositoryms.service.SocialService;
import com.smithy.g.repo.server.branch.model.CreateBranchBody;
import com.smithy.g.repo.server.collaborator.model.AddCollaboratorBody;
import com.smithy.g.repo.server.repository.model.CreateRepositoryBody;
import com.smithy.g.repo.server.repository.model.ForkRepositoryBody;
import com.smithy.g.repo.server.repository.model.UpdateRepositoryBody;
import com.smithy.g.repo.server.repository.model.RepositoryDTO;
import com.smithy.g.repo.server.collaborator.model.UpdateCollaboratorRoleBody;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class GrpcRepositoryServiceImpl extends RepoServiceGrpc.RepoServiceImplBase {

    private final RepositoryService repositoryService;
    private final BranchService branchService;
    private final CollaboratorService collaboratorService;
    private final SocialService socialService;
    private final GrpcProtoMapper mapper;

    // ─── Repositorios ─────────────────────────────────────────

    @Override
    public void createRepository(CreateRepositoryRequest req,
                                 StreamObserver<CreateRepositoryResponse> obs) {
        try {
            CreateRepositoryBody body = new CreateRepositoryBody()
                    .name(req.getName())
                    .description(req.getDescription().isEmpty() ? null : req.getDescription())
                    .visibility(req.getVisibility() == RepoVisibility.REPO_VISIBILITY_UNSPECIFIED
                        ? com.smithy.g.repo.server.repository.model.RepoVisibility.PUBLIC
                            : mapper.fromProtoVisibility(req.getVisibility()))
                    .language(req.getLanguage().isEmpty() ? null : req.getLanguage());
            var resp = repositoryService.createRepository(body);
            if (resp.getBody() == null) {
                obs.onError(Status.INTERNAL.withDescription("Failed to create repository").asRuntimeException());
                return;
            }
            obs.onNext(CreateRepositoryResponse.newBuilder()
                    .setRepository(mapper.toProtoRepository(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateRepository(UpdateRepositoryRequest req,
                                 StreamObserver<UpdateRepositoryResponse> obs) {
        try {
            UpdateRepositoryBody body = new UpdateRepositoryBody()
                    .description(req.getDescription().isEmpty() ? null : req.getDescription())
                    .language(req.getLanguage().isEmpty() ? null : req.getLanguage())
                    .hasIssues(req.getHasIssues());
            if (req.getVisibility() != RepoVisibility.REPO_VISIBILITY_UNSPECIFIED) {
                body.setVisibility(mapper.fromProtoVisibility(req.getVisibility()));
            }
            ResponseEntity<RepositoryDTO> resp = repositoryService.updateRepository(req.getOwner(), req.getRepo(), body);
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            obs.onNext(UpdateRepositoryResponse.newBuilder()
                    .setRepository(mapper.toProtoRepository(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteRepository(DeleteRepositoryRequest req,
                                 StreamObserver<DeleteRepositoryResponse> obs) {
        try {
            var resp = repositoryService.deleteRepository(req.getOwner(), req.getRepo());
            if (resp.getStatusCode().value() == 404) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            obs.onNext(DeleteRepositoryResponse.newBuilder().setSuccess(true).build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listMyRepositories(ListMyRepositoriesRequest req,
                                   StreamObserver<ListMyRepositoriesResponse> obs) {
        try {
                com.smithy.g.repo.server.repository.model.RepoVisibility visibility =
                    req.getVisibility() == RepoVisibility.REPO_VISIBILITY_UNSPECIFIED
                            ? null
                            : mapper.fromProtoVisibility(req.getVisibility());
            int page = req.getPagination().getPage() > 0 ? req.getPagination().getPage() : 1;
            int perPage = req.getPagination().getPerPage() > 0 ? req.getPagination().getPerPage() : 30;

            var resp = repositoryService.listMyRepositories(
                    visibility,
                    BigDecimal.valueOf(page),
                    BigDecimal.valueOf(perPage));
            if (resp.getBody() == null) {
                obs.onError(Status.INTERNAL.withDescription("Failed to list repositories").asRuntimeException());
                return;
            }
            List<com.githubx.grpc.proto.RepositoryDTO> repos = resp.getBody().getRepositories()
                    .stream()
                    .map(mapper::toProtoRepository)
                    .toList();
            obs.onNext(ListMyRepositoriesResponse.newBuilder()
                    .addAllRepositories(repos)
                    .setPagination(mapper.toProtoPagination(resp.getBody().getPagination()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void forkRepository(ForkRepositoryRequest req,
                               StreamObserver<ForkRepositoryResponse> obs) {
        try {
            ForkRepositoryBody body = new ForkRepositoryBody()
                    .targetOwner(req.getTargetOwner().isEmpty() ? null : req.getTargetOwner())
                    .name(req.getName().isEmpty() ? null : req.getName());
            var resp = repositoryService.forkRepository(req.getOwner(), req.getRepo(), body);
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            obs.onNext(ForkRepositoryResponse.newBuilder()
                    .setRepository(mapper.toProtoRepository(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ─── Ramas ────────────────────────────────────────────────

    @Override
    public void createBranch(CreateBranchRequest req, StreamObserver<CreateBranchResponse> obs) {
        try {
            CreateBranchBody body = new CreateBranchBody().name(req.getName());
            var resp = branchService.createBranch(req.getOwner(), req.getRepo(), body);
            if (resp.getStatusCode().value() == 404) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            if (resp.getStatusCode().value() == 409) {
                obs.onError(Status.ALREADY_EXISTS.withDescription("Branch already exists").asRuntimeException());
                return;
            }
            if (resp.getBody() == null) {
                obs.onError(Status.INTERNAL.withDescription("Failed to create branch").asRuntimeException());
                return;
            }
            obs.onNext(CreateBranchResponse.newBuilder()
                    .setBranch(mapper.toProtoBranch(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteBranch(DeleteBranchRequest req, StreamObserver<DeleteBranchResponse> obs) {
        try {
            var resp = branchService.deleteBranch(req.getOwner(), req.getRepo(), req.getBranch());
            if (resp.getStatusCode().value() == 404) {
                obs.onError(Status.NOT_FOUND.withDescription("Branch not found").asRuntimeException());
                return;
            }
            if (resp.getStatusCode().value() == 409) {
                obs.onError(Status.FAILED_PRECONDITION.withDescription("Cannot delete the default branch").asRuntimeException());
                return;
            }
            obs.onNext(DeleteBranchResponse.newBuilder().setSuccess(true).build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ─── Colaboradores ────────────────────────────────────────

    @Override
    public void addCollaborator(AddCollaboratorRequest req,
                                StreamObserver<AddCollaboratorResponse> obs) {
        try {
                com.smithy.g.repo.server.collaborator.model.CollaboratorRole role =
                    req.getRole() == CollaboratorRole.COLLABORATOR_ROLE_UNSPECIFIED
                            ? com.smithy.g.repo.server.collaborator.model.CollaboratorRole.READ
                            : mapper.fromProtoCollaboratorRole(req.getRole());
            AddCollaboratorBody body = new AddCollaboratorBody()
                    .username(req.getUsername())
                    .role(role);
            var resp = collaboratorService.addCollaboratorWithRole(req.getOwner(), req.getRepo(), body);
            if (resp.getStatusCode().value() == 404) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            if (resp.getStatusCode().value() == 409) {
                obs.onError(Status.ALREADY_EXISTS.withDescription("Collaborator already exists").asRuntimeException());
                return;
            }
            if (resp.getBody() == null) {
                obs.onError(Status.INTERNAL.withDescription("Failed to add collaborator").asRuntimeException());
                return;
            }
            obs.onNext(AddCollaboratorResponse.newBuilder()
                    .setCollaborator(mapper.toProtoCollaborator(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getCollaborator(GetCollaboratorRequest req,
                                StreamObserver<GetCollaboratorResponse> obs) {
        try {
            var resp = collaboratorService.getCollaborator(req.getOwner(), req.getRepo(), req.getUsername());
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Collaborator not found").asRuntimeException());
                return;
            }
            obs.onNext(GetCollaboratorResponse.newBuilder()
                    .setCollaborator(mapper.toProtoCollaborator(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listCollaborators(ListCollaboratorsRequest req,
                                  StreamObserver<ListCollaboratorsResponse> obs) {
        try {
            var resp = collaboratorService.listCollaborators(req.getOwner(), req.getRepo());
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            List<com.githubx.grpc.proto.CollaboratorDTO> collaborators = resp.getBody().getCollaborators()
                    .stream()
                    .map(mapper::toProtoCollaborator)
                    .toList();
            obs.onNext(ListCollaboratorsResponse.newBuilder()
                    .addAllCollaborators(collaborators)
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void removeCollaborator(RemoveCollaboratorRequest req,
                                   StreamObserver<RemoveCollaboratorResponse> obs) {
        try {
            var resp = collaboratorService.removeCollaborator(req.getOwner(), req.getRepo(), req.getUsername());
            if (resp.getStatusCode().value() == 404) {
                obs.onError(Status.NOT_FOUND.withDescription("Collaborator not found").asRuntimeException());
                return;
            }
            obs.onNext(RemoveCollaboratorResponse.newBuilder().setSuccess(true).build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateCollaboratorRole(UpdateCollaboratorRoleRequest req,
                                       StreamObserver<UpdateCollaboratorRoleResponse> obs) {
        try {
            UpdateCollaboratorRoleBody body = new UpdateCollaboratorRoleBody()
                    .role(mapper.fromProtoCollaboratorRole(req.getRole()));
            var resp = collaboratorService.updateCollaboratorRole(
                    req.getOwner(), req.getRepo(), req.getUsername(), body);
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Collaborator not found").asRuntimeException());
                return;
            }
            obs.onNext(UpdateCollaboratorRoleResponse.newBuilder()
                    .setCollaborator(mapper.toProtoCollaborator(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ─── Social ───────────────────────────────────────────────

    @Override
    public void starRepository(StarRepositoryRequest req,
                               StreamObserver<StarRepositoryResponse> obs) {
        try {
            var resp = socialService.starRepositoryForAuthenticatedUser(req.getOwner(), req.getRepo());
            if (resp.getStatusCode().value() == 404) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            obs.onNext(StarRepositoryResponse.newBuilder().setSuccess(true).build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void unstarRepository(UnstarRepositoryRequest req,
                                 StreamObserver<UnstarRepositoryResponse> obs) {
        try {
            var resp = socialService.unstarRepositoryForAuthenticatedUser(req.getOwner(), req.getRepo());
            if (resp.getStatusCode().value() == 404) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            obs.onNext(UnstarRepositoryResponse.newBuilder().setSuccess(true).build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
