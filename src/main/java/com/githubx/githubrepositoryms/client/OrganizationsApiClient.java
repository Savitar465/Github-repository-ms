package com.githubx.githubrepositoryms.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class OrganizationsApiClient {

    private final RestClient restClient;
    private final String baseUrl;

    public OrganizationsApiClient(
            @Value("${app.services.organizations-ms.url:http://localhost:8085}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        log.info("OrganizationsApiClient initialized with baseUrl: {}", baseUrl);
    }

    public RepoAccessResponse getRepoAccess(String owner, String repo) {
        String uri = "/v1/repos/" + owner + "/" + repo + "/access/teams";
        log.info("Fetching repo access from organizations-ms: {}/{}", owner, repo);

        try {
            RepoAccessResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        log.warn("Repo access not found: {}/{}, status: {}", owner, repo, clientResponse.getStatusCode());
                    })
                    .body(RepoAccessResponse.class);

            log.info("Repo access fetched successfully: {} teams",
                    response != null && response.getTeams() != null ? response.getTeams().size() : 0);
            return response;
        } catch (Exception e) {
            log.warn("Error fetching repo access from organizations-ms: {}/{} - {}", owner, repo, e.getMessage());
            return new RepoAccessResponse();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepoAccessResponse {
        private List<TeamAccessDTO> teams;
        private List<CollaboratorDTO> collaborators;

        public RepoAccessResponse() {
            this.teams = List.of();
            this.collaborators = List.of();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamAccessDTO {
        private String teamId;
        private String teamName;
        private String orgName;
        private String permission;
        private List<TeamMemberDTO> members;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamMemberDTO {
        private String userId;
        private String username;
        private String avatarUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CollaboratorDTO {
        private String userId;
        private String username;
        private String avatarUrl;
        private String role;
    }
}
