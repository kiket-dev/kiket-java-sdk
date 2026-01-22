package dev.kiket.sdk.responses;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard response format for extension handlers.
 *
 * <p>Use the static factory methods to build properly formatted responses:
 * <ul>
 *   <li>{@link #allow()} - Build an allow response</li>
 *   <li>{@link #deny(String)} - Build a deny response</li>
 *   <li>{@link #pending(String)} - Build a pending response</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Simple allow
 * return ExtensionResponse.allow()
 *     .message("Successfully configured")
 *     .build();
 *
 * // Allow with output fields
 * return ExtensionResponse.allow()
 *     .message("Successfully configured Mailjet")
 *     .data("routeId", 123)
 *     .outputField("inbound_email", "abc@parse.example.com")
 *     .build();
 *
 * // Deny with error details
 * return ExtensionResponse.deny("Invalid credentials")
 *     .data("errorCode", "AUTH_FAILED")
 *     .build();
 * }</pre>
 */
public class ExtensionResponse {

    private final String status;
    private final String message;
    private final Map<String, Object> metadata;

    private ExtensionResponse(String status, String message, Map<String, Object> metadata) {
        this.status = status;
        this.message = message;
        this.metadata = Collections.unmodifiableMap(metadata);
    }

    /**
     * Get the response status.
     *
     * @return status (allow, deny, or pending)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the response message.
     *
     * @return message or null if not set
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the response metadata.
     *
     * @return unmodifiable metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Convert to a Map for JSON serialization.
     *
     * @return response as a Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        if (message != null) {
            result.put("message", message);
        }
        result.put("metadata", metadata);
        return result;
    }

    /**
     * Start building an allow response.
     *
     * @return builder for allow response
     */
    public static AllowBuilder allow() {
        return new AllowBuilder();
    }

    /**
     * Start building a deny response.
     *
     * @param message reason for denial (required)
     * @return builder for deny response
     */
    public static ResponseBuilder deny(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Deny response requires a message");
        }
        return new ResponseBuilder("deny", message);
    }

    /**
     * Start building a pending response.
     *
     * @param message status message (required)
     * @return builder for pending response
     */
    public static ResponseBuilder pending(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Pending response requires a message");
        }
        return new ResponseBuilder("pending", message);
    }

    /**
     * Builder for allow responses with output fields support.
     */
    public static class AllowBuilder {
        private String message;
        private final Map<String, Object> data = new HashMap<>();
        private final Map<String, String> outputFields = new HashMap<>();

        AllowBuilder() {}

        /**
         * Set an optional success message.
         *
         * @param message success message
         * @return this builder
         */
        public AllowBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Add data to the response metadata.
         *
         * @param key metadata key
         * @param value metadata value
         * @return this builder
         */
        public AllowBuilder data(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        /**
         * Add multiple data entries to the response metadata.
         *
         * @param data map of metadata entries
         * @return this builder
         */
        public AllowBuilder data(Map<String, Object> data) {
            if (data != null) {
                this.data.putAll(data);
            }
            return this;
        }

        /**
         * Add an output field to be displayed in the configuration UI.
         *
         * @param key field key (must match manifest output_fields schema)
         * @param value field value
         * @return this builder
         */
        public AllowBuilder outputField(String key, String value) {
            this.outputFields.put(key, value);
            return this;
        }

        /**
         * Add multiple output fields to be displayed in the configuration UI.
         *
         * @param fields map of output field key-value pairs
         * @return this builder
         */
        public AllowBuilder outputFields(Map<String, String> fields) {
            if (fields != null) {
                this.outputFields.putAll(fields);
            }
            return this;
        }

        /**
         * Build the response.
         *
         * @return the built ExtensionResponse
         */
        public ExtensionResponse build() {
            Map<String, Object> metadata = new HashMap<>(data);
            if (!outputFields.isEmpty()) {
                metadata.put("output_fields", new HashMap<>(outputFields));
            }
            return new ExtensionResponse("allow", message, metadata);
        }
    }

    /**
     * Builder for deny and pending responses.
     */
    public static class ResponseBuilder {
        private final String status;
        private final String message;
        private final Map<String, Object> data = new HashMap<>();

        ResponseBuilder(String status, String message) {
            this.status = status;
            this.message = message;
        }

        /**
         * Add data to the response metadata.
         *
         * @param key metadata key
         * @param value metadata value
         * @return this builder
         */
        public ResponseBuilder data(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        /**
         * Add multiple data entries to the response metadata.
         *
         * @param data map of metadata entries
         * @return this builder
         */
        public ResponseBuilder data(Map<String, Object> data) {
            if (data != null) {
                this.data.putAll(data);
            }
            return this;
        }

        /**
         * Build the response.
         *
         * @return the built ExtensionResponse
         */
        public ExtensionResponse build() {
            return new ExtensionResponse(status, message, data);
        }
    }
}
