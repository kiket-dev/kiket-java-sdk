package dev.kiket.sdk.config;

import dev.kiket.sdk.telemetry.TelemetryReporter;

import java.util.Map;

/**
 * SDK configuration.
 */
public class SDKConfig {
    private String webhookSecret;
    private String workspaceToken;
    private String baseUrl;
    private Map<String, Object> settings;
    private String extensionId;
    private String extensionVersion;
    private boolean telemetryEnabled;
    private TelemetryReporter.FeedbackHook feedbackHook;
    private String telemetryUrl;

    private SDKConfig(Builder builder) {
        this.webhookSecret = builder.webhookSecret;
        this.workspaceToken = builder.workspaceToken;
        this.baseUrl = builder.baseUrl;
        this.settings = builder.settings;
        this.extensionId = builder.extensionId;
        this.extensionVersion = builder.extensionVersion;
        this.telemetryEnabled = builder.telemetryEnabled;
        this.feedbackHook = builder.feedbackHook;
        this.telemetryUrl = builder.telemetryUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    public String getWorkspaceToken() { return workspaceToken; }
    public void setWorkspaceToken(String workspaceToken) { this.workspaceToken = workspaceToken; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public Map<String, Object> getSettings() { return settings; }
    public void setSettings(Map<String, Object> settings) { this.settings = settings; }

    public String getExtensionId() { return extensionId; }
    public void setExtensionId(String extensionId) { this.extensionId = extensionId; }

    public String getExtensionVersion() { return extensionVersion; }
    public void setExtensionVersion(String extensionVersion) { this.extensionVersion = extensionVersion; }

    public boolean isTelemetryEnabled() { return telemetryEnabled; }
    public void setTelemetryEnabled(boolean telemetryEnabled) { this.telemetryEnabled = telemetryEnabled; }

    public TelemetryReporter.FeedbackHook getFeedbackHook() { return feedbackHook; }
    public void setFeedbackHook(TelemetryReporter.FeedbackHook feedbackHook) { this.feedbackHook = feedbackHook; }

    public String getTelemetryUrl() { return telemetryUrl; }
    public void setTelemetryUrl(String telemetryUrl) { this.telemetryUrl = telemetryUrl; }

    public static class Builder {
        private String webhookSecret;
        private String workspaceToken;
        private String baseUrl;
        private Map<String, Object> settings;
        private String extensionId;
        private String extensionVersion;
        private boolean telemetryEnabled;
        private TelemetryReporter.FeedbackHook feedbackHook;
        private String telemetryUrl;

        public Builder webhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
            return this;
        }

        public Builder workspaceToken(String workspaceToken) {
            this.workspaceToken = workspaceToken;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
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

        public Builder telemetryEnabled(boolean telemetryEnabled) {
            this.telemetryEnabled = telemetryEnabled;
            return this;
        }

        public Builder feedbackHook(TelemetryReporter.FeedbackHook feedbackHook) {
            this.feedbackHook = feedbackHook;
            return this;
        }

        public Builder telemetryUrl(String telemetryUrl) {
            this.telemetryUrl = telemetryUrl;
            return this;
        }

        public SDKConfig build() {
            return new SDKConfig(this);
        }
    }
}
