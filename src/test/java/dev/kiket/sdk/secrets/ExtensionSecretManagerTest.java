package dev.kiket.sdk.secrets;

import dev.kiket.sdk.client.KiketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ExtensionSecretManagerTest {

    @Mock
    private KiketClient client;

    private ExtensionSecretManager secretManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        secretManager = new ExtensionSecretManager(client, "test-extension");
    }

    @Test
    void testGet() {
        when(client.get(eq("/extensions/test-extension/secrets/API_KEY"), eq(Map.class)))
            .thenReturn(Mono.just(Map.of("value", "secret-value")));

        String result = secretManager.get("API_KEY");

        assertEquals("secret-value", result);
    }

    @Test
    void testGetNotFound() {
        when(client.get(eq("/extensions/test-extension/secrets/MISSING"), eq(Map.class)))
            .thenReturn(Mono.error(new RuntimeException("404")));

        String result = secretManager.get("MISSING");

        assertNull(result);
    }

    @Test
    void testSet() {
        when(client.post(eq("/extensions/test-extension/secrets/API_KEY"), any(), eq(Map.class)))
            .thenReturn(Mono.just(Map.of()));

        assertDoesNotThrow(() -> secretManager.set("API_KEY", "new-value"));

        verify(client).post(
            eq("/extensions/test-extension/secrets/API_KEY"),
            eq(Map.of("value", "new-value")),
            eq(Map.class)
        );
    }

    @Test
    void testDelete() {
        when(client.delete(eq("/extensions/test-extension/secrets/API_KEY"), eq(Map.class)))
            .thenReturn(Mono.just(Map.of()));

        assertDoesNotThrow(() -> secretManager.delete("API_KEY"));

        verify(client).delete(
            eq("/extensions/test-extension/secrets/API_KEY"),
            eq(Map.class)
        );
    }

    @Test
    void testList() {
        when(client.get(eq("/extensions/test-extension/secrets"), eq(Map.class)))
            .thenReturn(Mono.just(Map.of("keys", List.of("API_KEY", "SECRET_TOKEN"))));

        List<String> result = secretManager.list();

        assertEquals(2, result.size());
        assertTrue(result.contains("API_KEY"));
        assertTrue(result.contains("SECRET_TOKEN"));
    }

    @Test
    void testRotate() {
        when(client.delete(eq("/extensions/test-extension/secrets/API_KEY"), eq(Map.class)))
            .thenReturn(Mono.just(Map.of()));
        when(client.post(eq("/extensions/test-extension/secrets/API_KEY"), any(), eq(Map.class)))
            .thenReturn(Mono.just(Map.of()));

        assertDoesNotThrow(() -> secretManager.rotate("API_KEY", "new-value"));

        verify(client).delete(eq("/extensions/test-extension/secrets/API_KEY"), eq(Map.class));
        verify(client).post(
            eq("/extensions/test-extension/secrets/API_KEY"),
            eq(Map.of("value", "new-value")),
            eq(Map.class)
        );
    }
}
