package com.githubx.githubrepositoryms.service;

import com.smithy.g.repo.server.repository.model.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

public interface RepositoryService {

    ResponseEntity<RepositoryDTO> createRepository(CreateRepositoryBody body);

    ResponseEntity<Void> deleteRepository(String owner, String repo);

    ResponseEntity<RepositoryDTO> getRepository(String owner, String repo);

    ResponseEntity<ListRepositoriesBody> listMyRepositories(RepoVisibility visibility,
                                                             BigDecimal page,
                                                             BigDecimal perPage);

    ResponseEntity<RepositoryDTO> updateRepository(String owner, String repo,
                                                    UpdateRepositoryBody body);

    ResponseEntity<RepositoryDTO> forkRepository(String owner, String repo,
                                                  ForkRepositoryBody body);

    ResponseEntity<ListRepositoryForksBody> listRepositoryForks(String owner, String repo);
}
