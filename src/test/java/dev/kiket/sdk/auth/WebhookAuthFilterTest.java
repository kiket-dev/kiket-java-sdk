package dev.kiket.sdk.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebhookAuthFilterTest {

    @BeforeEach
    void setUp() {
        WebhookAuthFilter.clearJwksCache();
    }

    @Test
    void testAuthenticationExceptionMessage() {
        var ex = new WebhookAuthFilter.AuthenticationException("test error");
        assertEquals("test error", ex.getMessage());
    }

    @Test
    void testAuthenticationExceptionWithCause() {
        var cause = new RuntimeException("underlying cause");
        var ex = new WebhookAuthFilter.AuthenticationException("test error", cause);
        assertEquals("test error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testIsAuthenticationError() {
        var authEx = new WebhookAuthFilter.AuthenticationException("test");
        assertTrue(WebhookAuthFilter.isAuthenticationError(authEx));

        var regularEx = new RuntimeException("not auth");
        assertFalse(WebhookAuthFilter.isAuthenticationError(regularEx));
    }

    @Test
    void testVerifyRuntimeToken_MissingAuthentication() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "test");

        var ex = assertThrows(WebhookAuthFilter.AuthenticationException.class, () ->
                WebhookAuthFilter.verifyRuntimeToken(payload, "https://kiket.dev"));
        assertEquals("Missing runtime_token in payload", ex.getMessage());
    }

    @Test
    void testVerifyRuntimeToken_MissingToken() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> auth = new HashMap<>();
        payload.put("authentication", auth);

        var ex = assertThrows(WebhookAuthFilter.AuthenticationException.class, () ->
                WebhookAuthFilter.verifyRuntimeToken(payload, "https://kiket.dev"));
        assertEquals("Missing runtime_token in payload", ex.getMessage());
    }

    @Test
    void testVerifyRuntimeToken_EmptyToken() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> auth = new HashMap<>();
        auth.put("runtime_token", "");
        payload.put("authentication", auth);

        var ex = assertThrows(WebhookAuthFilter.AuthenticationException.class, () ->
                WebhookAuthFilter.verifyRuntimeToken(payload, "https://kiket.dev"));
        assertEquals("Missing runtime_token in payload", ex.getMessage());
    }

    @Test
    void testBuildAuthContext() {
        long exp = Instant.now().plusSeconds(3600).getEpochSecond();
        var jwtPayload = new WebhookAuthFilter.JwtPayload(
                "test-subject",
                123,
                456,
                789,
                111,
                List.of("read", "write"),
                "webhook",
                "kiket.dev",
                System.currentTimeMillis() / 1000,
                exp,
                "unique-id"
        );

        Map<String, Object> rawPayload = new HashMap<>();
        Map<String, Object> auth = new HashMap<>();
        auth.put("runtime_token", "test-token");
        rawPayload.put("authentication", auth);

        var authCtx = WebhookAuthFilter.buildAuthContext(jwtPayload, rawPayload);

        assertEquals("test-token", authCtx.runtimeToken());
        assertEquals("runtime", authCtx.tokenType());
        assertEquals(123, authCtx.orgId());
        assertEquals(456, authCtx.extId());
        assertEquals(789, authCtx.projId());
        assertEquals(List.of("read", "write"), authCtx.scopes());
        assertNotNull(authCtx.expiresAt());
    }

    @Test
    void testBuildAuthContext_NullScopes() {
        var jwtPayload = new WebhookAuthFilter.JwtPayload(
                "test-subject",
                null,
                null,
                null,
                null,
                null,
                null,
                "kiket.dev",
                null,
                null,
                null
        );

        Map<String, Object> rawPayload = new HashMap<>();

        var authCtx = WebhookAuthFilter.buildAuthContext(jwtPayload, rawPayload);

        assertNotNull(authCtx.scopes());
        assertTrue(authCtx.scopes().isEmpty());
        assertNull(authCtx.expiresAt());
    }

    @Test
    void testBuildAuthContext_NoAuthentication() {
        var jwtPayload = new WebhookAuthFilter.JwtPayload(
                "test-subject",
                null,
                null,
                null,
                null,
                null,
                null,
                "kiket.dev",
                null,
                null,
                null
        );

        Map<String, Object> rawPayload = new HashMap<>();

        var authCtx = WebhookAuthFilter.buildAuthContext(jwtPayload, rawPayload);

        assertNull(authCtx.runtimeToken());
        assertEquals("runtime", authCtx.tokenType());
    }

    @Test
    void testClearJwksCache() {
        // Just verify it doesn't throw
        WebhookAuthFilter.clearJwksCache();
    }

    @Test
    void testJwtPayloadRecord() {
        var payload = new WebhookAuthFilter.JwtPayload(
                "sub",
                1,
                2,
                3,
                4,
                List.of("scope1"),
                "src",
                "iss",
                100L,
                200L,
                "jti"
        );

        assertEquals("sub", payload.sub());
        assertEquals(1, payload.orgId());
        assertEquals(2, payload.extId());
        assertEquals(3, payload.projId());
        assertEquals(4, payload.piId());
        assertEquals(List.of("scope1"), payload.scopes());
        assertEquals("src", payload.src());
        assertEquals("iss", payload.iss());
        assertEquals(100L, payload.iat());
        assertEquals(200L, payload.exp());
        assertEquals("jti", payload.jti());
    }

    @Test
    void testAuthContextRecord() {
        var now = Instant.now();
        var authCtx = new WebhookAuthFilter.AuthContext(
                "token",
                "runtime",
                now,
                List.of("read"),
                1,
                2,
                3
        );

        assertEquals("token", authCtx.runtimeToken());
        assertEquals("runtime", authCtx.tokenType());
        assertEquals(now, authCtx.expiresAt());
        assertEquals(List.of("read"), authCtx.scopes());
        assertEquals(1, authCtx.orgId());
        assertEquals(2, authCtx.extId());
        assertEquals(3, authCtx.projId());
    }
}
