package dev.kiket.sdk.endpoints;

import dev.kiket.sdk.client.KiketClient;
import dev.kiket.sdk.secrets.ExtensionSecretManager;

import java.util.Map;

/**
 * High-level extension endpoints.
 */
public class ExtensionEndpoints {
    private final KiketClient client;
    private final String extensionId;
    private final String eventVersion;
    private final ExtensionSecretManager secrets;

    public ExtensionEndpoints(KiketClient client, String extensionId, String eventVersion) {
        this.client = client;
        this.extensionId = extensionId;
        this.eventVersion = eventVersion;
        this.secrets = new ExtensionSecretManager(client, extensionId);
    }

    public ExtensionSecretManager getSecrets() {
        return secrets;
    }

    public void logEvent(String event, Map<String, Object> data) {
        Map<String, Object> payload = Map.of(
            "event", event,
            "version", eventVersion,
            "data", data,
            "timestamp", java.time.Instant.now().toString()
        );

        client.post("/extensions/" + extensionId + "/events", payload, Map.class)
            .block();
    }

    public Object getMetadata() {
        return client.get("/extensions/" + extensionId, Map.class)
            .block();
    }
}
