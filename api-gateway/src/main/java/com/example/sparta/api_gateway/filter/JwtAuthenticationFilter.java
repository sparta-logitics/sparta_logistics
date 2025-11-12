package com.example.sparta.api_gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        System.out.println("[JWT Filter] Request Path: " + path);

        if (path.startsWith("/v1/auth")) {
            System.out.println("[JWT Filter] Auth path, skipping JWT check");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        System.out.println("[JWT Filter] Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JWT Filter] No token or invalid format");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        System.out.println("[JWT Filter] Token: " + token);

        return Mono.fromCallable(() ->
                        Jwts.parserBuilder()
                                .setSigningKey(getSigningKey())
                                .build()
                                .parseClaimsJws(token)
                                .getBody()
                )
                .flatMap(claims -> {
                    String userId = claims.get("user_id", String.class);
                    String username = claims.get("username", String.class);
                    String role = claims.get("role", String.class);

                    System.out.println("[JWT Filter] Claims - userId: " + userId
                            + ", username: " + username
                            + ", role: " + role);

                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header("X-USERID", userId)
                            .header("X-USERNAME", username)
                            .header("X-USER-ROLE", role)
                            .build();

                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .onErrorResume(e -> {
                    System.out.println("[JWT Filter] JWT parsing failed: " + e.getMessage());
                    e.printStackTrace();
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}
