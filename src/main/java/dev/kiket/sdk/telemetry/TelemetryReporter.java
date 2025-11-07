package dev.kiket.sdk.telemetry;

import lombok.Data;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

/**
 * Telemetry reporter for SDK usage metrics.
 */
public class TelemetryReporter {
    private final boolean enabled;
    private final WebClient webClient;
    private final FeedbackHook feedbackHook;
    private final String extensionId;
    private final String extensionVersion;

    public TelemetryReporter(
        boolean enabled,
        String telemetryUrl,
        FeedbackHook feedbackHook,
        String extensionId,
        String extensionVersion
    ) {
        String optOut = System.getenv("KIKET_SDK_TELEMETRY_OPTOUT");
        this.enabled = enabled && !"1".equals(optOut);
        this.feedbackHook = feedbackHook;
        this.extensionId = extensionId;
        this.extensionVersion = extensionVersion;

        if (telemetryUrl != null) {
            this.webClient = WebClient.builder()
                .baseUrl(telemetryUrl)
                .build();
        } else {
            this.webClient = null;
        }
    }

    public void record(String event, String version, String status, double durationMs, String message) {
        if (!enabled) {
            return;
        }

        TelemetryRecord record = TelemetryRecord.builder()
            .event(event)
            .version(version)
            .status(status)
            .durationMs(durationMs)
            .message(message)
            .extensionId(extensionId)
            .extensionVersion(extensionVersion)
            .timestamp(Instant.now().toString())
            .build();

        // Call feedback hook
        if (feedbackHook != null) {
            try {
                feedbackHook.accept(record);
            } catch (Exception e) {
                System.err.println("Feedback hook failed: " + e.getMessage());
            }
        }

        // Send to telemetry URL
        if (webClient != null) {
            webClient.post()
                .uri("/telemetry")
                .bodyValue(record)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                    result -> {},
                    error -> System.err.println("Failed to send telemetry: " + error.getMessage())
                );
        }
    }

    @FunctionalInterface
    public interface FeedbackHook {
        void accept(TelemetryRecord record);
    }

    @Data
    @lombok.Builder
    public static class TelemetryRecord {
        private String event;
        private String version;
        private String status;
        private double durationMs;
        private String message;
        private String extensionId;
        private String extensionVersion;
        private String timestamp;
    }
}
