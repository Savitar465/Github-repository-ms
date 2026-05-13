package com.githubx.githubrepositoryms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmithyControllersConfig {

    @Bean("repositoryV1ApiController")
    public com.smithy.g.repo.server.repository.api.V1ApiController repositoryV1ApiController(
            @Autowired(required = false) com.smithy.g.repo.server.repository.api.V1ApiDelegate delegate) {
        return new com.smithy.g.repo.server.repository.api.V1ApiController(delegate);
    }

    @Bean("branchV1ApiController")
    public com.smithy.g.repo.server.branch.api.V1ApiController branchV1ApiController(
            @Autowired(required = false) com.smithy.g.repo.server.branch.api.V1ApiDelegate delegate) {
        return new com.smithy.g.repo.server.branch.api.V1ApiController(delegate);
    }

    @Bean("collaboratorV1ApiController")
    public com.smithy.g.repo.server.collaborator.api.V1ApiController collaboratorV1ApiController(
            @Autowired(required = false) com.smithy.g.repo.server.collaborator.api.V1ApiDelegate delegate) {
        return new com.smithy.g.repo.server.collaborator.api.V1ApiController(delegate);
    }

    @Bean("contentV1ApiController")
    public com.smithy.g.repo.server.content.api.V1ApiController contentV1ApiController(
            @Autowired(required = false) com.smithy.g.repo.server.content.api.V1ApiDelegate delegate) {
        return new com.smithy.g.repo.server.content.api.V1ApiController(delegate);
    }

    @Bean("socialV1ApiController")
    public com.smithy.g.repo.server.social.api.V1ApiController socialV1ApiController(
            @Autowired(required = false) com.smithy.g.repo.server.social.api.V1ApiDelegate delegate) {
        return new com.smithy.g.repo.server.social.api.V1ApiController(delegate);
    }
}
