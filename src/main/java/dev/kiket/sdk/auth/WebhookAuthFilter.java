package dev.kiket.sdk.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT verification for webhook payloads.
 * Verifies runtime tokens are signed by Kiket using ES256 (ECDSA P-256).
 */
public class WebhookAuthFilter {

    private static final String ALGORITHM = "ES256";
    private static final String ISSUER = "kiket.dev";
    private static final Duration JWKS_CACHE_TTL = Duration.ofHours(1);
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);

    private static final Map<String, JWKSCacheEntry> jwksCache = new ConcurrentHashMap<>();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private record JWKSCacheEntry(JWKSet jwks, Instant fetchedAt) {}

    /**
     * Authentication exception for JWT verification failures.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Decoded JWT payload.
     */
    public record JwtPayload(
            String sub,
            Integer orgId,
            Integer extId,
            Integer projId,
            Integer piId,
            List<String> scopes,
            String src,
            String iss,
            Long iat,
            Long exp,
            String jti
    ) {}

    /**
     * Authentication context from verified JWT.
     */
    public record AuthContext(
            String runtimeToken,
            String tokenType,
            Instant expiresAt,
            List<String> scopes,
            Integer orgId,
            Integer extId,
            Integer projId
    ) {}

    /**
     * Verify the runtime token JWT from the payload.
     *
     * @param payload The webhook payload containing authentication.runtime_token
     * @param baseUrl Base URL for fetching JWKS
     * @return The decoded JWT payload
     * @throws AuthenticationException if verification fails
     */
    @SuppressWarnings("unchecked")
    public static JwtPayload verifyRuntimeToken(Map<String, Object> payload, String baseUrl) {
        Map<String, Object> auth = (Map<String, Object>) payload.get("authentication");
        if (auth == null) {
            throw new AuthenticationException("Missing runtime_token in payload");
        }

        String token = (String) auth.get("runtime_token");
        if (token == null || token.isEmpty()) {
            throw new AuthenticationException("Missing runtime_token in payload");
        }

        return decodeJwt(token, baseUrl);
    }

    /**
     * Decode and verify a JWT token using the public key from JWKS.
     *
     * @param token The JWT token to verify
     * @param baseUrl Base URL for fetching JWKS
     * @return The decoded JWT payload
     * @throws AuthenticationException if verification fails
     */
    public static JwtPayload decodeJwt(String token, String baseUrl) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Verify algorithm
            if (!JWSAlgorithm.ES256.equals(signedJWT.getHeader().getAlgorithm())) {
                throw new AuthenticationException("Unexpected signing method: " + signedJWT.getHeader().getAlgorithm());
            }

            // Fetch JWKS and find signing key
            JWKSet jwks = fetchJwks(baseUrl);
            String kid = signedJWT.getHeader().getKeyID();
            ECKey ecKey = findSigningKey(jwks, kid);

            // Verify signature
            JWSVerifier verifier = new ECDSAVerifier(ecKey);
            if (!signedJWT.verify(verifier)) {
                throw new AuthenticationException("Invalid token signature");
            }

            // Validate claims
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            validateClaims(claims);

            return parseJwtPayload(claims);

        } catch (ParseException e) {
            throw new AuthenticationException("Invalid token format: " + e.getMessage(), e);
        } catch (JOSEException e) {
            throw new AuthenticationException("Token verification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch JWKS from the well-known endpoint with caching.
     *
     * @param baseUrl Base URL for fetching JWKS
     * @return The JWK Set
     * @throws AuthenticationException if fetching fails
     */
    public static JWKSet fetchJwks(String baseUrl) {
        JWKSCacheEntry cached = jwksCache.get(baseUrl);
        if (cached != null && Duration.between(cached.fetchedAt(), Instant.now()).compareTo(JWKS_CACHE_TTL) < 0) {
            return cached.jwks();
        }

        String jwksUrl = baseUrl.replaceAll("/$", "") + "/.well-known/jwks.json";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AuthenticationException("Failed to fetch JWKS: status " + response.statusCode());
            }

            JWKSet jwks = JWKSet.parse(response.body());
            jwksCache.put(baseUrl, new JWKSCacheEntry(jwks, Instant.now()));
            return jwks;

        } catch (IOException | InterruptedException e) {
            throw new AuthenticationException("Failed to fetch JWKS: " + e.getMessage(), e);
        } catch (ParseException e) {
            throw new AuthenticationException("Invalid JWKS response", e);
        }
    }

    /**
     * Clear the JWKS cache (useful for testing or key rotation).
     */
    public static void clearJwksCache() {
        jwksCache.clear();
    }

    /**
     * Build authentication context from verified JWT payload.
     *
     * @param jwtPayload The verified JWT payload
     * @param rawPayload The original webhook payload
     * @return The authentication context
     */
    @SuppressWarnings("unchecked")
    public static AuthContext buildAuthContext(JwtPayload jwtPayload, Map<String, Object> rawPayload) {
        Map<String, Object> auth = (Map<String, Object>) rawPayload.getOrDefault("authentication", Map.of());
        String runtimeToken = (String) auth.get("runtime_token");

        Instant expiresAt = jwtPayload.exp() != null ? Instant.ofEpochSecond(jwtPayload.exp()) : null;
        List<String> scopes = jwtPayload.scopes() != null ? jwtPayload.scopes() : List.of();

        return new AuthContext(
                runtimeToken,
                "runtime",
                expiresAt,
                scopes,
                jwtPayload.orgId(),
                jwtPayload.extId(),
                jwtPayload.projId()
        );
    }

    /**
     * Check if an exception is an authentication error.
     */
    public static boolean isAuthenticationError(Exception e) {
        return e instanceof AuthenticationException;
    }

    private static ECKey findSigningKey(JWKSet jwks, String kid) {
        for (JWK key : jwks.getKeys()) {
            if (!"sig".equals(key.getKeyUse().getValue())) {
                continue;
            }
            if (!JWSAlgorithm.ES256.equals(key.getAlgorithm())) {
                continue;
            }
            if (kid != null && !kid.isEmpty() && !kid.equals(key.getKeyID())) {
                continue;
            }
            if (key instanceof ECKey ecKey) {
                return ecKey;
            }
        }
        throw new AuthenticationException("No suitable signing key found in JWKS");
    }

    private static void validateClaims(JWTClaimsSet claims) {
        // Verify issuer
        if (!ISSUER.equals(claims.getIssuer())) {
            throw new AuthenticationException("Invalid token issuer");
        }

        // Verify expiration
        Date exp = claims.getExpirationTime();
        if (exp == null) {
            throw new AuthenticationException("Token missing expiration");
        }
        if (exp.before(new Date())) {
            throw new AuthenticationException("Runtime token has expired");
        }
    }

    @SuppressWarnings("unchecked")
    private static JwtPayload parseJwtPayload(JWTClaimsSet claims) {
        Integer orgId = claims.getClaim("org_id") != null ? ((Number) claims.getClaim("org_id")).intValue() : null;
        Integer extId = claims.getClaim("ext_id") != null ? ((Number) claims.getClaim("ext_id")).intValue() : null;
        Integer projId = claims.getClaim("proj_id") != null ? ((Number) claims.getClaim("proj_id")).intValue() : null;
        Integer piId = claims.getClaim("pi_id") != null ? ((Number) claims.getClaim("pi_id")).intValue() : null;

        List<String> scopes = null;
        Object scopesClaim = claims.getClaim("scopes");
        if (scopesClaim instanceof List<?> list) {
            scopes = list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        Long iat = claims.getIssueTime() != null ? claims.getIssueTime().getTime() / 1000 : null;
        Long exp = claims.getExpirationTime() != null ? claims.getExpirationTime().getTime() / 1000 : null;

        return new JwtPayload(
                claims.getSubject(),
                orgId,
                extId,
                projId,
                piId,
                scopes,
                (String) claims.getClaim("src"),
                claims.getIssuer(),
                iat,
                exp,
                claims.getJWTID()
        );
    }
}
