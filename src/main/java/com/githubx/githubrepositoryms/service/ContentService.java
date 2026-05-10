package com.githubx.githubrepositoryms.service;

import com.smithy.g.repo.server.content.model.FileEntryDTO;
import com.smithy.g.repo.server.content.model.GetRepoContentsBody;
import com.smithy.g.repo.server.content.model.UploadFileBody;
import org.springframework.http.ResponseEntity;

public interface ContentService {

    ResponseEntity<Void> deleteFile(String owner, String repo, String path,
                                     String message, String branch);

    ResponseEntity<GetRepoContentsBody> getRepoContents(String owner, String repo,
                                                         String path, String ref);

    ResponseEntity<FileEntryDTO> uploadFile(String owner, String repo, String path,
                                             UploadFileBody body);

    ResponseEntity<Void> downloadArchive(String owner, String repo, String ref);
}
