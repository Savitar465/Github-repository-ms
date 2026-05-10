package com.githubx.githubrepositoryms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

    private ApiInfo gitHubRepository = new ApiInfo();
    private ApiInfo gitHubBranch = new ApiInfo();
    private ApiInfo gitHubCollaborator = new ApiInfo();
    private ApiInfo gitHubContent = new ApiInfo();
    private ApiInfo gitHubSocial = new ApiInfo();

    @Data
    public static class ApiInfo {
        private String basePath = "";
        private String title;
        private String description;
        private String version;
    }
}
