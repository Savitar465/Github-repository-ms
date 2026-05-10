package com.githubx.githubrepositoryms.service;

import com.githubx.githubrepositoryms.dao.FileEntryDao;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.mapper.FileEntryMapper;
import com.githubx.githubrepositoryms.model.FileEntryDocument;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import com.smithy.g.repo.server.content.model.FileEntryDTO;
import com.smithy.g.repo.server.content.model.GetRepoContentsBody;
import com.smithy.g.repo.server.content.model.UploadFileBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final FileEntryDao fileEntryDao;
    private final RepositoryDao repositoryDao;
    private final FileEntryMapper fileEntryMapper;

    @Override
    public ResponseEntity<Void> deleteFile(String owner, String repo, String path,
                                            String message, String branch) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String targetBranch = branch != null ? branch : repoOpt.get().getDefaultBranch();
        if (fileEntryDao.findByRepositoryIdAndPathAndBranch(repoOpt.get().getId(), path, targetBranch).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        fileEntryDao.deleteByRepositoryIdAndPathAndBranch(repoOpt.get().getId(), path, targetBranch);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<GetRepoContentsBody> getRepoContents(String owner, String repo,
                                                                String path, String ref) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RepositoryDocument repoDoc = repoOpt.get();
        String targetBranch = ref != null ? ref : repoDoc.getDefaultBranch();
        List<FileEntryDTO> contents = fileEntryDao.findByRepositoryIdAndBranch(repoDoc.getId(), targetBranch)
                .stream()
                .filter(f -> f.getPath().startsWith(path))
                .map(fileEntryMapper::toDto)
                .toList();
        return ResponseEntity.ok(new GetRepoContentsBody(contents));
    }

    @Override
    public ResponseEntity<FileEntryDTO> uploadFile(String owner, String repo,
                                                    String path, UploadFileBody body) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repo);
        if (repoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RepositoryDocument repoDoc = repoOpt.get();
        String targetBranch = body.getBranch() != null ? body.getBranch() : repoDoc.getDefaultBranch();
        String fileName = Paths.get(path).getFileName().toString();
        byte[] decoded = Base64.getDecoder().decode(body.getContent());

        Optional<FileEntryDocument> existing = fileEntryDao
                .findByRepositoryIdAndPathAndBranch(repoDoc.getId(), path, targetBranch);

        FileEntryDocument entry = existing.orElseGet(() -> FileEntryDocument.builder()
                .id(UUID.randomUUID().toString())
                .repositoryId(repoDoc.getId())
                .build());

        entry.setName(fileName);
        entry.setPath(path);
        entry.setType("file");
        entry.setSize(decoded.length);
        entry.setContent(body.getContent());
        entry.setBranch(targetBranch);
        entry.setUpdatedAt(LocalDateTime.now());

        HttpStatus status = existing.isPresent() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(fileEntryMapper.toDto(fileEntryDao.save(entry)));
    }

    @Override
    public ResponseEntity<Void> downloadArchive(String owner, String repo, String ref) {
        if (repositoryDao.findByOwnerUsernameAndName(owner, repo).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
