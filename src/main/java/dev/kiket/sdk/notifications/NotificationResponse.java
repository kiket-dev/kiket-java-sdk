package dev.kiket.sdk.notifications;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard notification response from extension.
 */
public class NotificationResponse {
    private final boolean success;
    private final String messageId;
    private final Instant deliveredAt;
    private final String error;
    private final Integer retryAfter;

    private NotificationResponse(Builder builder) {
        this.success = builder.success;
        this.messageId = builder.messageId;
        this.deliveredAt = builder.deliveredAt;
        this.error = builder.error;
        this.retryAfter = builder.retryAfter;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessageId() { return messageId; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public String getError() { return error; }
    public Integer getRetryAfter() { return retryAfter; }

    /**
     * Convert to map for JSON serialization.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);

        if (messageId != null) {
            result.put("message_id", messageId);
        }

        if (deliveredAt != null) {
            result.put("delivered_at", deliveredAt.toString());
        }

        if (error != null) {
            result.put("error", error);
        }

        if (retryAfter != null) {
            result.put("retry_after", retryAfter);
        }

        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String messageId;
        private Instant deliveredAt;
        private String error;
        private Integer retryAfter;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder deliveredAt(Instant deliveredAt) {
            this.deliveredAt = deliveredAt;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder retryAfter(Integer retryAfter) {
            this.retryAfter = retryAfter;
            return this;
        }

        public NotificationResponse build() {
            return new NotificationResponse(this);
        }
    }
}
