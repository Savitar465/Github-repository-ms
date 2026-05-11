package com.githubx.githubrepositoryms.grpc;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Extrae el Bearer JWT de los metadatos gRPC e inyecta la autenticación en el SecurityContext
 * para que los servicios protegidos puedan acceder al usuario autenticado.
 */
@GrpcGlobalServerInterceptor
public class GrpcJwtInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTH_KEY =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final JwtDecoder jwtDecoder;

    public GrpcJwtInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        Authentication auth = extractAuth(headers);
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onHalfClose() {
                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                try {
                    super.onHalfClose();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }

            @Override
            public void onCancel() {
                SecurityContextHolder.clearContext();
                super.onCancel();
            }
        };
    }

    private Authentication extractAuth(Metadata headers) {
        String header = headers.get(AUTH_KEY);
        if (header == null || !header.startsWith("Bearer ")) return null;
        try {
            Jwt jwt = jwtDecoder.decode(header.substring(7));
            return new JwtAuthenticationToken(jwt);
        } catch (Exception e) {
            return null;
        }
    }
}
