package com.githubx.githubrepositoryms.controller;

import com.githubx.githubrepositoryms.service.RepositoryService;
import com.smithy.g.repo.server.repository.api.V1Api;
import com.smithy.g.repo.server.repository.api.V1ApiDelegate;
import jakarta.annotation.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Controller
@RequestMapping("${openapi.gitHubRepository.base-path:}")
public class RepositoryController implements V1Api{
    private final RepositoryService delegate;

    public RepositoryController(@Autowired(required = false) RepositoryService delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new RepositoryService() {});
    }

    @Override
    public V1ApiDelegate getDelegate() {
        return delegate;
    }


}
