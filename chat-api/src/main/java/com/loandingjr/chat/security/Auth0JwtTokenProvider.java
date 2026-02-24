package com.loandingjr.chat.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.loandingjr.chat.shared.exception.InvalidAuthenticationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class Auth0JwtTokenProvider {

    private static final String ISSUER = "Chat_API";

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private long jwtExpirationInMs;

    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC512(jwtSecret);
    }

    public String generateToken(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new InvalidAuthenticationException("Invalid authentication principal");
        }
        return generateToken(userDetails);
    }

    public String generateToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtExpirationInMs);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var jwtBuilder = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userDetails.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("roles", roles);

        if (userDetails.getId() != null) {
            jwtBuilder.withClaim("id", userDetails.getId());
        }

        return jwtBuilder.sign(algorithm);
    }

    public String getUsernameFromToken(String token) {
        if (token != null) {
            return validateToken(token);
        }
        return null;
    }

    public String validateToken(String token) {
        try {
            var verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);

            return verifier.getSubject();
        } catch (JWTVerificationException ex) {
            throw new InvalidAuthenticationException("Invalid JWT token: " + ex.getMessage());
        }
    }
}