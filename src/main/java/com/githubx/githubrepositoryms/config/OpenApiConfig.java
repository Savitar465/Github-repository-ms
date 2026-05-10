package com.githubx.githubrepositoryms.config;

import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final OpenApiProperties props;

    @Bean
    public GroupedOpenApi repositoryApi() {
        return GroupedOpenApi.builder()
                .group("repository")
                .displayName(props.getGitHubRepository().getTitle())
                .packagesToScan("com.githubx.githubrepositoryms.controller.repository")
                .addOpenApiCustomizer(api -> api.info(info(props.getGitHubRepository())))
                .build();
    }

    @Bean
    public GroupedOpenApi branchApi() {
        return GroupedOpenApi.builder()
                .group("branch")
                .displayName(props.getGitHubBranch().getTitle())
                .packagesToScan("com.githubx.githubrepositoryms.controller.branch")
                .addOpenApiCustomizer(api -> api.info(info(props.getGitHubBranch())))
                .build();
    }

    @Bean
    public GroupedOpenApi collaboratorApi() {
        return GroupedOpenApi.builder()
                .group("collaborator")
                .displayName(props.getGitHubCollaborator().getTitle())
                .packagesToScan("com.githubx.githubrepositoryms.controller.collaborator")
                .addOpenApiCustomizer(api -> api.info(info(props.getGitHubCollaborator())))
                .build();
    }

    @Bean
    public GroupedOpenApi contentApi() {
        return GroupedOpenApi.builder()
                .group("content")
                .displayName(props.getGitHubContent().getTitle())
                .packagesToScan("com.githubx.githubrepositoryms.controller.content")
                .addOpenApiCustomizer(api -> api.info(info(props.getGitHubContent())))
                .build();
    }

    @Bean
    public GroupedOpenApi socialApi() {
        return GroupedOpenApi.builder()
                .group("social")
                .displayName(props.getGitHubSocial().getTitle())
                .packagesToScan("com.githubx.githubrepositoryms.controller.social")
                .addOpenApiCustomizer(api -> api.info(info(props.getGitHubSocial())))
                .build();
    }

    private Info info(OpenApiProperties.ApiInfo apiInfo) {
        return new Info()
                .title(apiInfo.getTitle())
                .description(apiInfo.getDescription())
                .version(apiInfo.getVersion());
    }
}
