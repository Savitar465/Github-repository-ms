package com.githubx.githubrepositoryms.controller.content;

import com.githubx.githubrepositoryms.service.ContentService;
import com.smithy.g.repo.server.content.api.V1Api;
import com.smithy.g.repo.server.content.model.FileEntryDTO;
import com.smithy.g.repo.server.content.model.GetRepoContentsBody;
import com.smithy.g.repo.server.content.model.UploadFileBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${openapi.gitHubContent.base-path:}")
@RequiredArgsConstructor
public class ContentController implements V1Api {

    private final ContentService contentService;

    @Override
    public ResponseEntity<Void> deleteFile(String owner, String repo, String path,
                                            String message, String branch) {
        return contentService.deleteFile(owner, repo, path, message, branch);
    }

    @Override
    public ResponseEntity<GetRepoContentsBody> getRepoContents(String owner, String repo,
                                                                String path, String ref) {
        return contentService.getRepoContents(owner, repo, path, ref);
    }

    @Override
    public ResponseEntity<FileEntryDTO> uploadFile(String owner, String repo, String path,
                                                    UploadFileBody uploadFileBody) {
        return contentService.uploadFile(owner, repo, path, uploadFileBody);
    }

    @Override
    public ResponseEntity<Void> downloadArchive(String owner, String repo, String ref) {
        return contentService.downloadArchive(owner, repo, ref);
    }
}
