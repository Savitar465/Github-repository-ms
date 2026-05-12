package com.githubx.githubrepositoryms.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    public String getUsername() {
        Jwt jwt = getJwt();
        if (jwt == null) return "anonymous";
        String preferred = jwt.getClaimAsString("preferred_username");
        return preferred != null ? preferred : jwt.getSubject();
    }

    public String getUserId() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getSubject() : null;
    }

    public String getEmail() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaimAsString("email") : null;
    }

    private Jwt getJwt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }
}
