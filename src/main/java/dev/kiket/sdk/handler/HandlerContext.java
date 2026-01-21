package dev.kiket.sdk.handler;

import dev.kiket.sdk.client.KiketClient;
import dev.kiket.sdk.endpoints.ExtensionEndpoints;
import dev.kiket.sdk.secrets.ExtensionSecretManager;

import java.util.Collections;
import java.util.Map;

/**
 * Context passed to webhook handlers.
 */
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
    private Map<String, String> payloadSecrets = Collections.emptyMap();

    private HandlerContext(Builder builder) {
        this.event = builder.event;
        this.eventVersion = builder.eventVersion;
        this.headers = builder.headers;
        this.client = builder.client;
        this.endpoints = builder.endpoints;
        this.settings = builder.settings;
        this.extensionId = builder.extensionId;
        this.extensionVersion = builder.extensionVersion;
        this.secrets = builder.secrets;
        this.payloadSecrets = builder.payloadSecrets != null ? builder.payloadSecrets : Collections.emptyMap();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getEventVersion() { return eventVersion; }
    public void setEventVersion(String eventVersion) { this.eventVersion = eventVersion; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public KiketClient getClient() { return client; }
    public void setClient(KiketClient client) { this.client = client; }

    public ExtensionEndpoints getEndpoints() { return endpoints; }
    public void setEndpoints(ExtensionEndpoints endpoints) { this.endpoints = endpoints; }

    public Map<String, Object> getSettings() { return settings; }
    public void setSettings(Map<String, Object> settings) { this.settings = settings; }

    public String getExtensionId() { return extensionId; }
    public void setExtensionId(String extensionId) { this.extensionId = extensionId; }

    public String getExtensionVersion() { return extensionVersion; }
    public void setExtensionVersion(String extensionVersion) { this.extensionVersion = extensionVersion; }

    public ExtensionSecretManager getSecrets() { return secrets; }
    public void setSecrets(ExtensionSecretManager secrets) { this.secrets = secrets; }

    public Map<String, String> getPayloadSecrets() { return payloadSecrets; }
    public void setPayloadSecrets(Map<String, String> payloadSecrets) { this.payloadSecrets = payloadSecrets; }

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

    public static class Builder {
        private String event;
        private String eventVersion;
        private Map<String, String> headers;
        private KiketClient client;
        private ExtensionEndpoints endpoints;
        private Map<String, Object> settings;
        private String extensionId;
        private String extensionVersion;
        private ExtensionSecretManager secrets;
        private Map<String, String> payloadSecrets = Collections.emptyMap();

        public Builder event(String event) {
            this.event = event;
            return this;
        }

        public Builder eventVersion(String eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder client(KiketClient client) {
            this.client = client;
            return this;
        }

        public Builder endpoints(ExtensionEndpoints endpoints) {
            this.endpoints = endpoints;
            return this;
        }

        public Builder settings(Map<String, Object> settings) {
            this.settings = settings;
            return this;
        }

        public Builder extensionId(String extensionId) {
            this.extensionId = extensionId;
            return this;
        }

        public Builder extensionVersion(String extensionVersion) {
            this.extensionVersion = extensionVersion;
            return this;
        }

        public Builder secrets(ExtensionSecretManager secrets) {
            this.secrets = secrets;
            return this;
        }

        public Builder payloadSecrets(Map<String, String> payloadSecrets) {
            this.payloadSecrets = payloadSecrets;
            return this;
        }

        public HandlerContext build() {
            return new HandlerContext(this);
        }
    }
}
