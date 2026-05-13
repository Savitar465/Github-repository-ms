package com.githubx.githubrepositoryms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_MATCHERS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/actuator/**",
            // Git proxy endpoints (for clone/push operations)
            "/*/*.git/**",
            "/*/*.git"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/branches").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/branches/{branch}").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/contents").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/contents/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/file").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/forks").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/commits").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/commits/{sha}").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/repos/{owner}/{repo}/collaborators").permitAll()
                .requestMatchers(HttpMethod.GET, "/repos/{owner}/{repo}/compare").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
