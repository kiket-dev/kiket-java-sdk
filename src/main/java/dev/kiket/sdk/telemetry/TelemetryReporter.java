package dev.kiket.sdk.telemetry;

import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Telemetry reporter for SDK usage metrics.
 */
public class TelemetryReporter {
    private final boolean enabled;
    private final WebClient webClient;
    private final String endpoint;
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

        if (telemetryUrl != null && !telemetryUrl.isBlank()) {
            this.endpoint = normalizeEndpoint(telemetryUrl);
            this.webClient = WebClient.builder().build();
        } else {
            this.endpoint = null;
            this.webClient = null;
        }
    }

    public void record(String event, String version, String status, double durationMs, String message) {
        record(event, version, status, durationMs, message, null);
    }

    public void record(String event, String version, String status, double durationMs, String message, String errorClass) {
        if (!enabled) {
            return;
        }

        TelemetryRecord record = TelemetryRecord.builder()
            .event(event)
            .version(version)
            .status(status)
            .durationMs(durationMs)
            .errorMessage(message)
            .errorClass(errorClass)
            .extensionId(extensionId)
            .extensionVersion(extensionVersion)
            .timestamp(Instant.now().toString())
            .metadata(new HashMap<>())
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
        if (webClient != null && endpoint != null) {
            webClient.post()
                .uri(endpoint)
                .bodyValue(buildPayload(record))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                    result -> {},
                    error -> System.err.println("Failed to send telemetry: " + error.getMessage())
                );
        }
    }

    private static String normalizeEndpoint(String telemetryUrl) {
        String trimmed = telemetryUrl.replaceAll("/+$", "");
        if (trimmed.endsWith("/telemetry")) {
            return trimmed;
        }
        return trimmed + "/telemetry";
    }

    private static Map<String, Object> buildPayload(TelemetryRecord record) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", record.getEvent());
        payload.put("version", record.getVersion());
        payload.put("status", record.getStatus());
        payload.put("duration_ms", Math.round(record.getDurationMs()));
        payload.put("timestamp", record.getTimestamp());
        payload.put("extension_id", record.getExtensionId());
        payload.put("extension_version", record.getExtensionVersion());
        payload.put("error_message", record.getErrorMessage());
        payload.put("error_class", record.getErrorClass());
        payload.put("metadata", record.getMetadata() != null ? record.getMetadata() : new HashMap<>());
        return payload;
    }

    @FunctionalInterface
    public interface FeedbackHook {
        void accept(TelemetryRecord record);
    }

    public static class TelemetryRecord {
        private String event;
        private String version;
        private String status;
        private double durationMs;
        private String errorMessage;
        private String errorClass;
        private String extensionId;
        private String extensionVersion;
        private String timestamp;
        private Map<String, Object> metadata;

        private TelemetryRecord(Builder builder) {
            this.event = builder.event;
            this.version = builder.version;
            this.status = builder.status;
            this.durationMs = builder.durationMs;
            this.errorMessage = builder.errorMessage;
            this.errorClass = builder.errorClass;
            this.extensionId = builder.extensionId;
            this.extensionVersion = builder.extensionVersion;
            this.timestamp = builder.timestamp;
            this.metadata = builder.metadata;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getEvent() { return event; }
        public String getVersion() { return version; }
        public String getStatus() { return status; }
        public double getDurationMs() { return durationMs; }
        public String getErrorMessage() { return errorMessage; }
        public String getErrorClass() { return errorClass; }
        public String getExtensionId() { return extensionId; }
        public String getExtensionVersion() { return extensionVersion; }
        public String getTimestamp() { return timestamp; }
        public Map<String, Object> getMetadata() { return metadata; }

        public static class Builder {
            private String event;
            private String version;
            private String status;
            private double durationMs;
            private String errorMessage;
            private String errorClass;
            private String extensionId;
            private String extensionVersion;
            private String timestamp;
            private Map<String, Object> metadata;

            public Builder event(String event) { this.event = event; return this; }
            public Builder version(String version) { this.version = version; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder durationMs(double durationMs) { this.durationMs = durationMs; return this; }
            public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
            public Builder errorClass(String errorClass) { this.errorClass = errorClass; return this; }
            public Builder extensionId(String extensionId) { this.extensionId = extensionId; return this; }
            public Builder extensionVersion(String extensionVersion) { this.extensionVersion = extensionVersion; return this; }
            public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
            public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }

            public TelemetryRecord build() {
                return new TelemetryRecord(this);
            }
        }
    }
}
