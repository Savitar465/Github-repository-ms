package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.mapper.RepositoryMapper;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.smithy.g.repo.server.repository.model.*;
import lombok.RequiredArgsConstructor;
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

    private static final String DEFAULT_OWNER = "Anonymous";

    private final RepositoryDao repositoryDao;
    private final RepositoryMapper repositoryMapper;

    @Override
    public ResponseEntity<RepositoryDTO> createRepository(CreateRepositoryBody body) {
        RepositoryDocument doc = RepositoryDocument.builder()
                .id(UUID.randomUUID().toString())
                .name(body.getName())
                .fullName(DEFAULT_OWNER + "/" + body.getName())
                .description(body.getDescription())
                .visibility(body.getVisibility().name())
                .language(body.getLanguage())
                .hasIssues(true)
                .starsCount(0)
                .forksCount(0)
                .defaultBranch("main")
                .ownerUsername(DEFAULT_OWNER)
                .ownerId(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(repositoryMapper.toDto(repositoryDao.save(doc)));
    }

    @Override
    public ResponseEntity<Void> deleteRepository(String owner, String repo) {
        var found = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repositoryDao.deleteById(found.get().getId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RepositoryDTO> getRepository(String owner, String repo) {
        var found = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repositoryMapper.toDto(found.get()));
    }

    @Override
    public ResponseEntity<ListRepositoriesBody> listMyRepositories(RepoVisibility visibility,
                                                                    BigDecimal page,
                                                                    BigDecimal perPage) {
        List<RepositoryDocument> docs = visibility != null
                ? repositoryDao.findByOwnerUsernameAndVisibility(DEFAULT_OWNER, visibility.name())
                : repositoryDao.findByOwnerUsername(DEFAULT_OWNER);

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
        String forkOwner = body.getTargetOwner() != null ? body.getTargetOwner() : DEFAULT_OWNER;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(repositoryMapper.toDto(repositoryDao.save(fork)));
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
}
