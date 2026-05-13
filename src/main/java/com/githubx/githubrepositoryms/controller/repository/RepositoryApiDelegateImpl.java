package com.githubx.githubrepositoryms.controller.repository;

import com.githubx.githubrepositoryms.service.RepositoryService;
import com.smithy.g.repo.server.repository.api.V1ApiDelegate;
import com.smithy.g.repo.server.repository.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RepositoryApiDelegateImpl implements V1ApiDelegate {

    private final RepositoryService repositoryService;

    @Override
    public ResponseEntity<RepositoryDTO> createRepository(CreateRepositoryBody createRepositoryBody) {
        return repositoryService.createRepository(createRepositoryBody);
    }

    @Override
    public ResponseEntity<Void> deleteRepository(String owner, String repo) {
        return repositoryService.deleteRepository(owner, repo);
    }

    @Override
    public ResponseEntity<RepositoryDTO> getRepository(String owner, String repo) {
        return repositoryService.getRepository(owner, repo);
    }

    @Override
    public ResponseEntity<ListRepositoriesBody> listMyRepositories(
            RepoVisibility visibility,
            BigDecimal page,
            BigDecimal perPage) {
        return repositoryService.listMyRepositories(visibility, page, perPage);
    }

    @Override
    public ResponseEntity<RepositoryDTO> updateRepository(
            String owner,
            String repo,
            UpdateRepositoryBody updateRepositoryBody) {
        return repositoryService.updateRepository(owner, repo, updateRepositoryBody);
    }

    @Override
    public ResponseEntity<RepositoryDTO> forkRepository(
            String owner,
            String repo,
            ForkRepositoryBody forkRepositoryBody) {
        return repositoryService.forkRepository(owner, repo, forkRepositoryBody);
    }

    @Override
    public ResponseEntity<ListRepositoryForksBody> listRepositoryForks(
            String owner,
            String repo) {
        return repositoryService.listRepositoryForks(owner, repo);
    }

    @Override
    public ResponseEntity<SearchRepositoriesBody> searchRepositories(
            String q,
            BigDecimal page,
            BigDecimal perPage) {
        return repositoryService.searchRepositories(q, page, perPage);
    }

    @Override
    public ResponseEntity<ListPublicRepositoriesBody> listPublicRepositories(
            BigDecimal page,
            BigDecimal perPage,
            String sort) {
        return repositoryService.listPublicRepositories(page, perPage, sort);
    }
}
