package com.githubx.githubrepositoryms.service;

import com.smithy.g.repo.server.repository.api.V1ApiDelegate;
import com.smithy.g.repo.server.repository.model.CreateRepositoryBody;
import com.smithy.g.repo.server.repository.model.ForkRepositoryBody;
import com.smithy.g.repo.server.repository.model.ListRepositoriesBody;
import com.smithy.g.repo.server.repository.model.ListRepositoryForksBody;
import com.smithy.g.repo.server.repository.model.RepoVisibility;
import com.smithy.g.repo.server.repository.model.RepositoryDTO;
import com.smithy.g.repo.server.repository.model.UpdateRepositoryBody;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class RepositoryService implements V1ApiDelegate {

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return V1ApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<RepositoryDTO> createRepository(CreateRepositoryBody createRepositoryBody) {
        return V1ApiDelegate.super.createRepository(createRepositoryBody);
    }

    @Override
    public ResponseEntity<Void> deleteRepository(String owner, String repo) {
        return V1ApiDelegate.super.deleteRepository(owner, repo);
    }

    @Override
    public ResponseEntity<RepositoryDTO> forkRepository(String owner, String repo, ForkRepositoryBody forkRepositoryBody) {
        return V1ApiDelegate.super.forkRepository(owner, repo, forkRepositoryBody);
    }

    @Override
    public ResponseEntity<RepositoryDTO> getRepository(String owner, String repo) {
        return V1ApiDelegate.super.getRepository(owner, repo);
    }

    @Override
    public ResponseEntity<ListRepositoriesBody> listMyRepositories(RepoVisibility visibility, BigDecimal page, BigDecimal perPage) {
        return V1ApiDelegate.super.listMyRepositories(visibility, page, perPage);
    }

    @Override
    public ResponseEntity<ListRepositoryForksBody> listRepositoryForks(String owner, String repo) {
        return V1ApiDelegate.super.listRepositoryForks(owner, repo);
    }

    @Override
    public ResponseEntity<RepositoryDTO> updateRepository(String owner, String repo, UpdateRepositoryBody updateRepositoryBody) {
        return V1ApiDelegate.super.updateRepository(owner, repo, updateRepositoryBody);
    }
}
