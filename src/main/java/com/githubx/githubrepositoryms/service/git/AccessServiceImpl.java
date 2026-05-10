package com.githubx.githubrepositoryms.service.git;

import com.githubx.githubrepositoryms.dao.CollaboratorDao;
import com.githubx.githubrepositoryms.dao.RepositoryDao;
import com.githubx.githubrepositoryms.model.RepositoryDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {

    private static final Set<String> WRITE_ROLES = Set.of("WRITE", "ADMIN");

    private final RepositoryDao repositoryDao;
    private final CollaboratorDao collaboratorDao;

    @Override
    public boolean canRead(String username, String owner, String repoName) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repoName);
        if (repoOpt.isEmpty()) return false;
        RepositoryDocument repo = repoOpt.get();
        if ("PUBLIC".equalsIgnoreCase(repo.getVisibility())) return true;
        if (owner.equals(username)) return true;
        return collaboratorDao.existsByRepositoryIdAndUsername(repo.getId(), username);
    }

    @Override
    public boolean canWrite(String username, String owner, String repoName) {
        Optional<RepositoryDocument> repoOpt = repositoryDao.findByOwnerUsernameAndName(owner, repoName);
        if (repoOpt.isEmpty()) return false;
        RepositoryDocument repo = repoOpt.get();
        if (owner.equals(username)) return true;
        return collaboratorDao.findByRepositoryIdAndUsername(repo.getId(), username)
                .map(c -> WRITE_ROLES.contains(c.getRole()))
                .orElse(false);
    }
}
