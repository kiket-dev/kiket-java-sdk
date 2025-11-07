package dev.kiket.sdk.config;

import dev.kiket.sdk.telemetry.TelemetryReporter;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * SDK configuration.
 */
@Data
@Builder
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
}
