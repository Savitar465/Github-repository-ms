package com.githubx.githubrepositoryms.grpc;

import com.githubx.grpc.proto.*;
import com.githubx.githubrepositoryms.service.BranchService;
import com.githubx.githubrepositoryms.service.RepositoryService;
import com.smithy.g.repo.server.branch.model.BranchDTO;
import com.smithy.g.repo.server.repository.model.RepositoryDTO;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.http.ResponseEntity;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class GrpcRepositoryPublicServiceImpl extends RepoPublicServiceGrpc.RepoPublicServiceImplBase {

    private final RepositoryService repositoryService;
    private final BranchService branchService;
    private final GrpcProtoMapper mapper;

    @Override
    public void getRepository(GetRepositoryRequest req, StreamObserver<GetRepositoryResponse> obs) {
        try {
            ResponseEntity<RepositoryDTO> resp = repositoryService.getRepository(req.getOwner(), req.getRepo());
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            obs.onNext(GetRepositoryResponse.newBuilder()
                    .setRepository(mapper.toProtoRepository(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listRepositoryForks(ListRepositoryForksRequest req,
                                    StreamObserver<ListRepositoryForksResponse> obs) {
        try {
            var resp = repositoryService.listRepositoryForks(req.getOwner(), req.getRepo());
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            List<com.githubx.grpc.proto.RepositoryDTO> forks = resp.getBody().getRepositories()
                    .stream()
                    .map(mapper::toProtoRepository)
                    .toList();
            obs.onNext(ListRepositoryForksResponse.newBuilder()
                    .addAllForks(forks)
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listBranches(ListBranchesRequest req, StreamObserver<ListBranchesResponse> obs) {
        try {
            var resp = branchService.listBranches(req.getOwner(), req.getRepo());
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Repository not found").asRuntimeException());
                return;
            }
            List<com.githubx.grpc.proto.BranchDTO> branches = resp.getBody().getBranches()
                    .stream()
                    .map(mapper::toProtoBranch)
                    .toList();
            obs.onNext(ListBranchesResponse.newBuilder()
                    .addAllBranches(branches)
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getBranch(GetBranchRequest req, StreamObserver<GetBranchResponse> obs) {
        try {
            ResponseEntity<BranchDTO> resp = branchService.getBranch(req.getOwner(), req.getRepo(), req.getBranch());
            if (resp.getStatusCode().value() == 404 || resp.getBody() == null) {
                obs.onError(Status.NOT_FOUND.withDescription("Branch not found").asRuntimeException());
                return;
            }
            obs.onNext(GetBranchResponse.newBuilder()
                    .setBranch(mapper.toProtoBranch(resp.getBody()))
                    .build());
            obs.onCompleted();
        } catch (Exception e) {
            obs.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
