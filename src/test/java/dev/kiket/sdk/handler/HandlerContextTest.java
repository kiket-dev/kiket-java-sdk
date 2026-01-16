package dev.kiket.sdk.handler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HandlerContextTest {

    private Map<String, String> originalEnv;

    @BeforeEach
    void setUp() {
        originalEnv = new HashMap<>(System.getenv());
    }

    @AfterEach
    void tearDown() {
        // Note: Can't easily restore env vars in Java, tests should use unique keys
    }

    @Test
    void secret_returnsPayloadValueWhenPresent() {
        Map<String, String> payloadSecrets = new HashMap<>();
        payloadSecrets.put("TEST_KEY", "payload-value");

        HandlerContext context = HandlerContext.builder()
            .event("test")
            .eventVersion("v1")
            .headers(Collections.emptyMap())
            .settings(Collections.emptyMap())
            .payloadSecrets(payloadSecrets)
            .client(null)
            .endpoints(null)
            .secrets(null)
            .build();

        assertEquals("payload-value", context.secret("TEST_KEY"));
    }

    @Test
    void secret_fallsBackToEnvWhenPayloadMissing() throws Exception {
        // Set environment variable using reflection (for testing)
        setEnv("HANDLER_TEST_ENV_KEY", "env-value");

        HandlerContext context = HandlerContext.builder()
            .event("test")
            .eventVersion("v1")
            .headers(Collections.emptyMap())
            .settings(Collections.emptyMap())
            .payloadSecrets(Collections.emptyMap())
            .client(null)
            .endpoints(null)
            .secrets(null)
            .build();

        assertEquals("env-value", context.secret("HANDLER_TEST_ENV_KEY"));

        // Clean up
        removeEnv("HANDLER_TEST_ENV_KEY");
    }

    @Test
    void secret_returnsNullWhenNotFound() {
        HandlerContext context = HandlerContext.builder()
            .event("test")
            .eventVersion("v1")
            .headers(Collections.emptyMap())
            .settings(Collections.emptyMap())
            .payloadSecrets(Collections.emptyMap())
            .client(null)
            .endpoints(null)
            .secrets(null)
            .build();

        assertNull(context.secret("NONEXISTENT_KEY_12345"));
    }

    @Test
    void secret_payloadTakesPriorityOverEnv() throws Exception {
        setEnv("PRIORITY_TEST_KEY", "env-value");

        Map<String, String> payloadSecrets = new HashMap<>();
        payloadSecrets.put("PRIORITY_TEST_KEY", "payload-value");

        HandlerContext context = HandlerContext.builder()
            .event("test")
            .eventVersion("v1")
            .headers(Collections.emptyMap())
            .settings(Collections.emptyMap())
            .payloadSecrets(payloadSecrets)
            .client(null)
            .endpoints(null)
            .secrets(null)
            .build();

        assertEquals("payload-value", context.secret("PRIORITY_TEST_KEY"));

        removeEnv("PRIORITY_TEST_KEY");
    }

    @Test
    void secret_ignoresEmptyPayloadValue() throws Exception {
        setEnv("EMPTY_TEST_KEY", "env-value");

        Map<String, String> payloadSecrets = new HashMap<>();
        payloadSecrets.put("EMPTY_TEST_KEY", "");

        HandlerContext context = HandlerContext.builder()
            .event("test")
            .eventVersion("v1")
            .headers(Collections.emptyMap())
            .settings(Collections.emptyMap())
            .payloadSecrets(payloadSecrets)
            .client(null)
            .endpoints(null)
            .secrets(null)
            .build();

        assertEquals("env-value", context.secret("EMPTY_TEST_KEY"));

        removeEnv("EMPTY_TEST_KEY");
    }

    @Test
    void secret_handlesNullPayloadSecrets() throws Exception {
        setEnv("NULL_MAP_TEST_KEY", "env-value");

        HandlerContext context = HandlerContext.builder()
            .event("test")
            .eventVersion("v1")
            .headers(Collections.emptyMap())
            .settings(Collections.emptyMap())
            .payloadSecrets(null)
            .client(null)
            .endpoints(null)
            .secrets(null)
            .build();

        assertEquals("env-value", context.secret("NULL_MAP_TEST_KEY"));

        removeEnv("NULL_MAP_TEST_KEY");
    }

    // Helper methods to manipulate environment variables in tests
    @SuppressWarnings("unchecked")
    private static void setEnv(String key, String value) throws Exception {
        Map<String, String> env = System.getenv();
        Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        ((Map<String, String>) field.get(env)).put(key, value);
    }

    @SuppressWarnings("unchecked")
    private static void removeEnv(String key) throws Exception {
        Map<String, String> env = System.getenv();
        Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        ((Map<String, String>) field.get(env)).remove(key);
    }
}
