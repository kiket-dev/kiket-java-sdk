package dev.kiket.sdk.responses;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionResponseTest {

    // Allow response tests

    @Test
    void allow_returnsProperlyFormattedResponse() {
        ExtensionResponse response = ExtensionResponse.allow().build();

        assertEquals("allow", response.getStatus());
        assertNull(response.getMessage());
        assertTrue(response.getMetadata().isEmpty());
    }

    @Test
    void allow_includesMessageWhenProvided() {
        ExtensionResponse response = ExtensionResponse.allow()
            .message("Success")
            .build();

        assertEquals("Success", response.getMessage());
    }

    @Test
    void allow_includesDataInMetadata() {
        ExtensionResponse response = ExtensionResponse.allow()
            .data("routeId", 123)
            .data("email", "test@example.com")
            .build();

        assertEquals(123, response.getMetadata().get("routeId"));
        assertEquals("test@example.com", response.getMetadata().get("email"));
    }

    @Test
    void allow_includesDataMapInMetadata() {
        Map<String, Object> data = new HashMap<>();
        data.put("routeId", 456);
        data.put("active", true);

        ExtensionResponse response = ExtensionResponse.allow()
            .data(data)
            .build();

        assertEquals(456, response.getMetadata().get("routeId"));
        assertEquals(true, response.getMetadata().get("active"));
    }

    @Test
    void allow_includesOutputFieldsInMetadata() {
        ExtensionResponse response = ExtensionResponse.allow()
            .outputField("inbound_email", "abc@parse.example.com")
            .build();

        @SuppressWarnings("unchecked")
        Map<String, String> outputFields = (Map<String, String>) response.getMetadata().get("output_fields");
        assertNotNull(outputFields);
        assertEquals("abc@parse.example.com", outputFields.get("inbound_email"));
    }

    @Test
    void allow_includesOutputFieldsMapInMetadata() {
        Map<String, String> fields = new HashMap<>();
        fields.put("webhook_url", "https://example.com/hook");
        fields.put("api_key", "sk-xxx");

        ExtensionResponse response = ExtensionResponse.allow()
            .outputFields(fields)
            .build();

        @SuppressWarnings("unchecked")
        Map<String, String> outputFields = (Map<String, String>) response.getMetadata().get("output_fields");
        assertEquals("https://example.com/hook", outputFields.get("webhook_url"));
        assertEquals("sk-xxx", outputFields.get("api_key"));
    }

    @Test
    void allow_combinesDataAndOutputFieldsInMetadata() {
        ExtensionResponse response = ExtensionResponse.allow()
            .message("Configured successfully")
            .data("routeId", 456)
            .outputField("webhook_url", "https://example.com/hook")
            .build();

        assertEquals("allow", response.getStatus());
        assertEquals("Configured successfully", response.getMessage());
        assertEquals(456, response.getMetadata().get("routeId"));

        @SuppressWarnings("unchecked")
        Map<String, String> outputFields = (Map<String, String>) response.getMetadata().get("output_fields");
        assertEquals("https://example.com/hook", outputFields.get("webhook_url"));
    }

    // Deny response tests

    @Test
    void deny_returnsProperlyFormattedResponse() {
        ExtensionResponse response = ExtensionResponse.deny("Access denied").build();

        assertEquals("deny", response.getStatus());
        assertEquals("Access denied", response.getMessage());
        assertTrue(response.getMetadata().isEmpty());
    }

    @Test
    void deny_includesDataInMetadata() {
        ExtensionResponse response = ExtensionResponse.deny("Invalid credentials")
            .data("errorCode", "AUTH_FAILED")
            .build();

        assertEquals("AUTH_FAILED", response.getMetadata().get("errorCode"));
    }

    @Test
    void deny_requiresMessage() {
        assertThrows(IllegalArgumentException.class, () ->
            ExtensionResponse.deny(null)
        );

        assertThrows(IllegalArgumentException.class, () ->
            ExtensionResponse.deny("")
        );
    }

    // Pending response tests

    @Test
    void pending_returnsProperlyFormattedResponse() {
        ExtensionResponse response = ExtensionResponse.pending("Awaiting approval").build();

        assertEquals("pending", response.getStatus());
        assertEquals("Awaiting approval", response.getMessage());
        assertTrue(response.getMetadata().isEmpty());
    }

    @Test
    void pending_includesDataInMetadata() {
        ExtensionResponse response = ExtensionResponse.pending("Processing")
            .data("jobId", "abc123")
            .build();

        assertEquals("abc123", response.getMetadata().get("jobId"));
    }

    @Test
    void pending_requiresMessage() {
        assertThrows(IllegalArgumentException.class, () ->
            ExtensionResponse.pending(null)
        );

        assertThrows(IllegalArgumentException.class, () ->
            ExtensionResponse.pending("")
        );
    }

    // toMap tests

    @Test
    void toMap_includesAllFieldsForAllow() {
        ExtensionResponse response = ExtensionResponse.allow()
            .message("Success")
            .data("key", "value")
            .outputField("field", "output")
            .build();

        Map<String, Object> map = response.toMap();

        assertEquals("allow", map.get("status"));
        assertEquals("Success", map.get("message"));

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) map.get("metadata");
        assertEquals("value", metadata.get("key"));

        @SuppressWarnings("unchecked")
        Map<String, String> outputFields = (Map<String, String>) metadata.get("output_fields");
        assertEquals("output", outputFields.get("field"));
    }

    @Test
    void toMap_omitsMessageWhenNull() {
        ExtensionResponse response = ExtensionResponse.allow().build();

        Map<String, Object> map = response.toMap();

        assertFalse(map.containsKey("message"));
    }

    @Test
    void toMap_includesAllFieldsForDeny() {
        ExtensionResponse response = ExtensionResponse.deny("Error occurred")
            .data("details", "some error")
            .build();

        Map<String, Object> map = response.toMap();

        assertEquals("deny", map.get("status"));
        assertEquals("Error occurred", map.get("message"));

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) map.get("metadata");
        assertEquals("some error", metadata.get("details"));
    }

    // Immutability tests

    @Test
    void metadata_isImmutable() {
        ExtensionResponse response = ExtensionResponse.allow()
            .data("key", "value")
            .build();

        assertThrows(UnsupportedOperationException.class, () ->
            response.getMetadata().put("new", "entry")
        );
    }

    @Test
    void allow_handlesNullDataMap() {
        ExtensionResponse response = ExtensionResponse.allow()
            .data((Map<String, Object>) null)
            .build();

        assertTrue(response.getMetadata().isEmpty());
    }

    @Test
    void allow_handlesNullOutputFieldsMap() {
        ExtensionResponse response = ExtensionResponse.allow()
            .outputFields(null)
            .build();

        assertFalse(response.getMetadata().containsKey("output_fields"));
    }
}
