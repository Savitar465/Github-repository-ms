package com.githubx.githubrepositoryms.controller.repository;

import com.githubx.githubrepositoryms.service.RepositoryService;
import com.smithy.g.repo.server.repository.api.V1Api;
import com.smithy.g.repo.server.repository.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("${openapi.gitHubRepository.base-path:}")
@RequiredArgsConstructor
public class RepositoryController implements V1Api {

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
    public ResponseEntity<ListRepositoriesBody> listMyRepositories(RepoVisibility visibility,
                                                                    BigDecimal page,
                                                                    BigDecimal perPage) {
        return repositoryService.listMyRepositories(visibility, page, perPage);
    }

    @Override
    public ResponseEntity<RepositoryDTO> updateRepository(String owner, String repo,
                                                           UpdateRepositoryBody updateRepositoryBody) {
        return repositoryService.updateRepository(owner, repo, updateRepositoryBody);
    }

    @Override
    public ResponseEntity<RepositoryDTO> forkRepository(String owner, String repo,
                                                         ForkRepositoryBody forkRepositoryBody) {
        return repositoryService.forkRepository(owner, repo, forkRepositoryBody);
    }

    @Override
    public ResponseEntity<ListRepositoryForksBody> listRepositoryForks(String owner, String repo) {
        return repositoryService.listRepositoryForks(owner, repo);
    }
}
