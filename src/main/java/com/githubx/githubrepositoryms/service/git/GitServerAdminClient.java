package com.githubx.githubrepositoryms.service.git;

import com.githubx.githubrepositoryms.config.GitServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitServerAdminClient {

    private final GitServerProperties props;

    @Value("${microservice.auth-token}")
    private String authToken;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void createRepo(String owner, String name) {
        send(post(adminUrl("repos/" + encodePathSegment(owner) + "/" + encodePathSegment(name)), HttpRequest.BodyPublishers.noBody()),
                "create repo " + owner + "/" + name);
    }

    public void deleteRepo(String owner, String name) {
        send(delete(adminUrl("repos/" + encodePathSegment(owner) + "/" + encodePathSegment(name))),
                "delete repo " + owner + "/" + name);
    }

    public void forkRepo(String sourceOwner, String sourceName, String forkOwner, String forkName) {
        String url = adminUrl("repos/" + encodePathSegment(forkOwner) + "/" + encodePathSegment(forkName))
            + "?clone-from=" + encodeQueryValue(sourceOwner + "/" + sourceName);
        send(post(url, HttpRequest.BodyPublishers.noBody()),
                "fork repo " + sourceOwner + "/" + sourceName + " → " + forkOwner + "/" + forkName);
    }

    public void syncKeys(String username, String keyFileContent) {
        send(put(adminUrl("keys/" + encodePathSegment(username)), HttpRequest.BodyPublishers.ofString(keyFileContent)),
                "sync keys for " + username);
    }

    public void deleteKeys(String username) {
        send(delete(adminUrl("keys/" + encodePathSegment(username))),
                "delete keys for " + username);
    }

    private String adminUrl(String path) {
        return props.getServer().getHttpUrl() + "/admin/" + path;
    }

    private HttpRequest post(String url, HttpRequest.BodyPublisher body) {
        return authorized(HttpRequest.newBuilder(URI.create(url)).POST(body)).build();
    }

    private HttpRequest put(String url, HttpRequest.BodyPublisher body) {
        return authorized(HttpRequest.newBuilder(URI.create(url)).PUT(body)).build();
    }

    private HttpRequest delete(String url) {
        return authorized(HttpRequest.newBuilder(URI.create(url)).DELETE()).build();
    }

    private HttpRequest.Builder authorized(HttpRequest.Builder builder) {
        return builder.header("X-Admin-Token", authToken);
    }

    private String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void send(HttpRequest request, String operation) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.error("Git admin API error [{}]: {} {}", operation, response.statusCode(), response.body());
            } else {
                log.info("Git admin API [{}] → {}", operation, response.statusCode());
            }
        } catch (Exception e) {
            log.error("Git admin API unreachable [{}]: {}", operation, e.toString(), e);
        }
    }
}
