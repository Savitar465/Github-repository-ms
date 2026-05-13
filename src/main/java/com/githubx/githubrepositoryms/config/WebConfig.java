package com.githubx.githubrepositoryms.config;

import com.smithy.g.repo.server.repository.model.RepoVisibility;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToRepoVisibilityConverter());
    }

    /**
     * Converter para manejar RepoVisibility desde @RequestParam
     * Permite valores en minúsculas como "public" y "private"
     */
    private static class StringToRepoVisibilityConverter implements Converter<String, RepoVisibility> {
        @Override
        public RepoVisibility convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            return RepoVisibility.fromValue(source.toLowerCase());
        }
    }
}
