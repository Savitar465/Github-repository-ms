package com.githubx.githubrepositoryms.controller.git;

import com.githubx.githubrepositoryms.config.GitServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Enumeration;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GitProxyController {

    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "transfer-encoding", "upgrade", "content-length"
    );

    private final GitServerProperties props;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @RequestMapping("/{owner}/{repo:.+\\.git}/**")
    public ResponseEntity<StreamingResponseBody> proxy(
            HttpServletRequest req,
            @PathVariable String owner,
            @PathVariable String repo) throws Exception {

        String subPath = extractSubPath(req, owner, repo);
        String query = req.getQueryString() != null ? "?" + req.getQueryString() : "";
        String upstreamUrl = props.getServer().getHttpUrl()
                + "/git/" + owner + "/" + repo + subPath + query;

        log.debug("Git proxy {} {} → {}", req.getMethod(), req.getRequestURI(), upstreamUrl);

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(upstreamUrl));

        copyRequestHeaders(req, reqBuilder);

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            reqBuilder.POST(HttpRequest.BodyPublishers.ofInputStream(() -> {
                try {
                    return req.getInputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        } else {
            reqBuilder.GET();
        }

        HttpResponse<InputStream> upstream = httpClient.send(
                reqBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());

        HttpHeaders responseHeaders = new HttpHeaders();
        upstream.headers().map().forEach((key, values) -> {
            if (key != null && !HOP_BY_HOP.contains(key.toLowerCase())) {
                responseHeaders.addAll(key, values);
            }
        });

        StreamingResponseBody body = out -> upstream.body().transferTo(out);

        return ResponseEntity.status(upstream.statusCode())
                .headers(responseHeaders)
                .body(body);
    }

    private void copyRequestHeaders(HttpServletRequest req, HttpRequest.Builder builder) {
        Enumeration<String> names = req.getHeaderNames();
        if (names == null) return;
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!HOP_BY_HOP.contains(name.toLowerCase()) && !"host".equalsIgnoreCase(name)) {
                builder.header(name, req.getHeader(name));
            }
        }
    }

    private String extractSubPath(HttpServletRequest req, String owner, String repo) {
        String uri = req.getRequestURI();
        String prefix = "/" + owner + "/" + repo;
        return uri.startsWith(prefix) ? uri.substring(prefix.length()) : "";
    }
}
