package dev.kiket.sdk.handler;

import dev.kiket.sdk.client.KiketClient;
import dev.kiket.sdk.endpoints.ExtensionEndpoints;
import dev.kiket.sdk.secrets.ExtensionSecretManager;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

/**
 * Context passed to webhook handlers.
 */
@Data
@Builder
public class HandlerContext {
    private String event;
    private String eventVersion;
    private Map<String, String> headers;
    private KiketClient client;
    private ExtensionEndpoints endpoints;
    private Map<String, Object> settings;
    private String extensionId;
    private String extensionVersion;
    /** Secret manager for API-based secret operations. */
    private ExtensionSecretManager secrets;
    /** Payload secrets (per-org configuration bundled by SecretResolver). */
    @Builder.Default
    private Map<String, String> payloadSecrets = Collections.emptyMap();

    /**
     * Retrieves a secret value by key.
     * <p>
     * Checks payload secrets first (per-org configuration), then falls back to
     * environment variables (extension defaults).
     * </p>
     *
     * <pre>{@code
     * String slackToken = context.secret("SLACK_BOT_TOKEN");
     * // Returns payload.secrets["SLACK_BOT_TOKEN"] || System.getenv("SLACK_BOT_TOKEN")
     * }</pre>
     *
     * @param key the secret key
     * @return the secret value, or null if not found
     */
    public String secret(String key) {
        // Payload secrets (per-org) take priority over ENV (extension defaults)
        if (payloadSecrets != null && payloadSecrets.containsKey(key)) {
            String value = payloadSecrets.get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return System.getenv(key);
    }
}
