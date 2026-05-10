package com.githubx.githubrepositoryms.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GitServerProperties.class)
public class GitServerConfig {
}
