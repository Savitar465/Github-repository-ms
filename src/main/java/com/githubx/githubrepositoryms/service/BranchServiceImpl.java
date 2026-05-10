package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.dao.BranchDao;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.mapper.BranchMapper;
import com.githubx.githubrepositoryms.model.BranchDocument;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.smithy.g.repo.server.branch.model.BranchDTO;
import com.smithy.g.repo.server.branch.model.CreateBranchBody;
import com.smithy.g.repo.server.branch.model.ListBranchesBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchDao branchDao;
    private final RepositoryDao repositoryDao;
    private final BranchMapper branchMapper;

    @Override
    public ResponseEntity<BranchDTO> createBranch(String owner, String repo, CreateBranchBody body) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String repoId = repoOpt.get().getId();
        if (branchDao.findByRepositoryIdAndName(repoId, body.getName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        BranchDocument branch = BranchDocument.builder()
                .id(UUID.randomUUID().toString())
                .repositoryId(repoId)
                .name(body.getName())
                .isDefault(Boolean.FALSE)
                .commitSha(UUID.randomUUID().toString().replace("-", ""))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(branchMapper.toDto(branchDao.save(branch)));
    }

    @Override
    public ResponseEntity<Void> deleteBranch(String owner, String repo, String branch) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String repoId = repoOpt.get().getId();
        Optional<BranchDocument> found = branchDao.findByRepositoryIdAndName(repoId, branch);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (Boolean.TRUE.equals(found.get().getIsDefault())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        branchDao.deleteByRepositoryIdAndName(repoId, branch);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BranchDTO> getBranch(String owner, String repo, String branch) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<BranchDocument> found = branchDao.findByRepositoryIdAndName(repoOpt.get().getId(), branch);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(branchMapper.toDto(found.get()));
    }

    @Override
    public ResponseEntity<ListBranchesBody> listBranches(String owner, String repo) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<BranchDTO> branches = branchDao.findByRepositoryId(repoOpt.get().getId())
                .stream()
                .map(branchMapper::toDto)
                .toList();
        return ResponseEntity.ok(new ListBranchesBody(branches));
    }
}
