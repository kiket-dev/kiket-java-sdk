package dev.kiket.sdk.notifications;

import java.util.HashMap;
import java.util.Map;

/**
 * Response from channel validation.
 */
public class ChannelValidationResponse {
    private final boolean valid;
    private final String error;
    private final Map<String, Object> metadata;

    private ChannelValidationResponse(Builder builder) {
        this.valid = builder.valid;
        this.error = builder.error;
        this.metadata = builder.metadata;
    }

    public boolean isValid() {
        return valid;
    }

    public String getError() {
        return error;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Convert to map for JSON serialization.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);

        if (error != null) {
            result.put("error", error);
        }

        if (metadata != null) {
            result.put("metadata", metadata);
        }

        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean valid;
        private String error;
        private Map<String, Object> metadata;

        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ChannelValidationResponse build() {
            return new ChannelValidationResponse(this);
        }
    }
}
