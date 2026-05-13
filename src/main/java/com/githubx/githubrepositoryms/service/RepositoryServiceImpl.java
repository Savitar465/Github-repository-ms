package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.config.AuthContext;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.mapper.RepositoryMapper;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.githubx.githubrepositoryms.service.git.GitOpsService;
import com.smithy.g.repo.server.repository.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final AuthContext authContext;
    private final RepositoryDao repositoryDao;
    private final RepositoryMapper repositoryMapper;
    private final GitOpsService gitOpsService;

    @Override
    public ResponseEntity<RepositoryDTO> createRepository(CreateRepositoryBody body) {
        String username = authContext.getUsername();
        String userId = authContext.getUserId();
        RepositoryDocument doc = RepositoryDocument.builder()
                .id(UUID.randomUUID().toString())
                .name(body.getName())
                .fullName(username + "/" + body.getName())
                .description(body.getDescription())
                .visibility(body.getVisibility().name())
                .language(body.getLanguage())
                .hasIssues(true)
                .starsCount(0)
                .forksCount(0)
                .defaultBranch("main")
                .ownerUsername(username)
                .ownerId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        RepositoryDocument saved = repositoryDao.save(doc);
        gitOpsService.createBareRepo(saved.getOwnerUsername(), saved.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(repositoryMapper.toDto(saved));
    }

    @Override
    public ResponseEntity<Void> deleteRepository(String owner, String repo) {
        var found = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repositoryDao.deleteById(found.get().getId());
        gitOpsService.deleteBareRepo(owner, repo);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RepositoryDTO> getRepository(String owner, String repo) {
        var found = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        return found.map(repositoryDocument -> ResponseEntity.ok(repositoryMapper.toDto(repositoryDocument))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ListRepositoriesBody> listMyRepositories(RepoVisibility visibility,
                                                                    BigDecimal page,
                                                                    BigDecimal perPage) {
        String username = authContext.getUsername();
        List<RepositoryDocument> docs = visibility != null
                ? repositoryDao.findByOwnerUsernameAndVisibility(username, visibility.getValue())
                : repositoryDao.findByOwnerUsername(username);

        int pageNum = page != null ? page.intValue() : 1;
        int size = perPage != null ? perPage.intValue() : 30;

        List<RepositoryDTO> dtos = docs.stream()
                .skip((long) (pageNum - 1) * size)
                .limit(size)
                .map(repositoryMapper::toDto)
                .toList();

        PaginationMeta meta = new PaginationMeta()
                .page(BigDecimal.valueOf(pageNum))
                .perPage(BigDecimal.valueOf(size))
                .total(BigDecimal.valueOf(docs.size()));

        return ResponseEntity.ok(new ListRepositoriesBody(dtos, meta));
    }

    @Override
    public ResponseEntity<RepositoryDTO> updateRepository(String owner, String repo,
                                                           UpdateRepositoryBody body) {
        var found = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RepositoryDocument doc = found.get();
        if (body.getDescription() != null) doc.setDescription(body.getDescription());
        if (body.getVisibility() != null) doc.setVisibility(body.getVisibility().name());
        if (body.getHasIssues() != null) doc.setHasIssues(body.getHasIssues());
        if (body.getLanguage() != null) doc.setLanguage(body.getLanguage());
        doc.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(repositoryMapper.toDto(repositoryDao.save(doc)));
    }

    @Override
    public ResponseEntity<RepositoryDTO> forkRepository(String owner, String repo,
                                                         ForkRepositoryBody body) {
        var source = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (source.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RepositoryDocument original = source.get();
        String forkOwner = body.getTargetOwner() != null ? body.getTargetOwner() : authContext.getUsername();
        String forkName = body.getName() != null ? body.getName() : original.getName();

        RepositoryDocument fork = RepositoryDocument.builder()
                .id(UUID.randomUUID().toString())
                .name(forkName)
                .fullName(forkOwner + "/" + forkName)
                .description(original.getDescription())
                .visibility(original.getVisibility())
                .language(original.getLanguage())
                .hasIssues(original.isHasIssues())
                .starsCount(0)
                .forksCount(0)
                .defaultBranch(original.getDefaultBranch())
                .ownerId(UUID.randomUUID().toString())
                .ownerUsername(forkOwner)
                .forkedFromId(original.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        original.setForksCount(original.getForksCount() + 1);
        repositoryDao.save(original);
        RepositoryDocument savedFork = repositoryDao.save(fork);
        gitOpsService.forkBareRepo(owner, repo, forkOwner, forkName);
        return ResponseEntity.status(HttpStatus.CREATED).body(repositoryMapper.toDto(savedFork));
    }

    @Override
    public ResponseEntity<ListRepositoryForksBody> listRepositoryForks(String owner, String repo) {
        var source = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (source.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<RepositoryDTO> forks = repositoryDao.findByForkedFromId(source.get().getId())
                .stream()
                .map(repositoryMapper::toDto)
                .toList();
        return ResponseEntity.ok(new ListRepositoryForksBody(forks));
    }

    @Override
    public ResponseEntity<SearchRepositoriesBody> searchRepositories(String query, BigDecimal page, BigDecimal perPage) {
        int pageNum = page != null ? page.intValue() : 1;
        int size = perPage != null ? perPage.intValue() : 20;

        Page<RepositoryDocument> resultPage = repositoryDao.searchPublicRepositories(
                query,
                PageRequest.of(pageNum - 1, size)
        );

        List<RepositoryDTO> dtos = resultPage.getContent().stream()
                .map(repositoryMapper::toDto)
                .toList();

        PaginationMeta meta = new PaginationMeta()
                .page(BigDecimal.valueOf(pageNum))
                .perPage(BigDecimal.valueOf(size))
                .total(BigDecimal.valueOf(resultPage.getTotalElements()))
                .totalPages(BigDecimal.valueOf(resultPage.getTotalPages()));

        return ResponseEntity.ok(new SearchRepositoriesBody(dtos, meta));
    }

    @Override
    public ResponseEntity<ListPublicRepositoriesBody> listPublicRepositories(BigDecimal page, BigDecimal perPage, String sort) {
        int pageNum = page != null ? page.intValue() : 1;
        int size = perPage != null ? perPage.intValue() : 20;

        Sort sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("stars".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "starsCount");
        } else if ("updated".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "updatedAt");
        }

        Page<RepositoryDocument> resultPage = repositoryDao.findAllPublicRepositories(
                PageRequest.of(pageNum - 1, size, sorting)
        );

        List<RepositoryDTO> dtos = resultPage.getContent().stream()
                .map(repositoryMapper::toDto)
                .toList();

        PaginationMeta meta = new PaginationMeta()
                .page(BigDecimal.valueOf(pageNum))
                .perPage(BigDecimal.valueOf(size))
                .total(BigDecimal.valueOf(resultPage.getTotalElements()))
                .totalPages(BigDecimal.valueOf(resultPage.getTotalPages()));

        return ResponseEntity.ok(new ListPublicRepositoriesBody(dtos, meta));
    }
}
