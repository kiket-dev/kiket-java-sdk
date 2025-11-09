package dev.kiket.sdk.notifications;

import java.util.List;
import java.util.Map;

/**
 * Standard notification request for extension delivery.
 */
public class NotificationRequest {
    private final String message;
    private final String channelType;
    private final String channelId;
    private final String recipientId;
    private final String format;
    private final String priority;
    private final Map<String, Object> metadata;
    private final String threadId;
    private final List<Map<String, Object>> attachments;

    private NotificationRequest(Builder builder) {
        this.message = builder.message;
        this.channelType = builder.channelType;
        this.channelId = builder.channelId;
        this.recipientId = builder.recipientId;
        this.format = builder.format;
        this.priority = builder.priority;
        this.metadata = builder.metadata;
        this.threadId = builder.threadId;
        this.attachments = builder.attachments;

        validate();
    }

    private void validate() {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message content is required");
        }

        if (!List.of("channel", "dm", "group").contains(channelType)) {
            throw new IllegalArgumentException("Invalid channelType: " + channelType);
        }

        if ("dm".equals(channelType) && (recipientId == null || recipientId.isEmpty())) {
            throw new IllegalArgumentException("recipientId is required for channelType='dm'");
        }

        if ("channel".equals(channelType) && (channelId == null || channelId.isEmpty())) {
            throw new IllegalArgumentException("channelId is required for channelType='channel'");
        }

        if (!List.of("plain", "markdown", "html").contains(format)) {
            throw new IllegalArgumentException("Invalid format: " + format);
        }

        if (!List.of("low", "normal", "high", "urgent").contains(priority)) {
            throw new IllegalArgumentException("Invalid priority: " + priority);
        }
    }

    // Getters
    public String getMessage() { return message; }
    public String getChannelType() { return channelType; }
    public String getChannelId() { return channelId; }
    public String getRecipientId() { return recipientId; }
    public String getFormat() { return format; }
    public String getPriority() { return priority; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getThreadId() { return threadId; }
    public List<Map<String, Object>> getAttachments() { return attachments; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private String channelType;
        private String channelId;
        private String recipientId;
        private String format = "markdown";
        private String priority = "normal";
        private Map<String, Object> metadata;
        private String threadId;
        private List<Map<String, Object>> attachments;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder channelType(String channelType) {
            this.channelType = channelType;
            return this;
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder recipientId(String recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder threadId(String threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder attachments(List<Map<String, Object>> attachments) {
            this.attachments = attachments;
            return this;
        }

        public NotificationRequest build() {
            return new NotificationRequest(this);
        }
    }
}
