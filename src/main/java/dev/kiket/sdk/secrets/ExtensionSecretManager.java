package dev.kiket.sdk.secrets;

import dev.kiket.sdk.client.KiketClient;

import java.util.List;
import java.util.Map;

/**
 * Extension secret manager.
 */
public class ExtensionSecretManager {
    private final KiketClient client;
    private final String extensionId;

    public ExtensionSecretManager(KiketClient client, String extensionId) {
        this.client = client;
        this.extensionId = extensionId;
    }

    public String get(String key) {
        try {
            Map<String, String> response = client
                .get("/extensions/" + extensionId + "/secrets/" + key, Map.class)
                .block();
            return response != null ? response.get("value") : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void set(String key, String value) {
        client.post(
            "/extensions/" + extensionId + "/secrets/" + key,
            Map.of("value", value),
            Map.class
        ).block();
    }

    public void delete(String key) {
        client.delete("/extensions/" + extensionId + "/secrets/" + key, Map.class)
            .block();
    }

    public List<String> list() {
        Map<String, List<String>> response = client
            .get("/extensions/" + extensionId + "/secrets", Map.class)
            .block();
        return response != null ? response.get("keys") : List.of();
    }

    public void rotate(String key, String newValue) {
        delete(key);
        set(key, newValue);
    }
}
