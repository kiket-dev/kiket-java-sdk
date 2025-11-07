package dev.kiket.sdk.handler;

import dev.kiket.sdk.client.KiketClient;
import dev.kiket.sdk.endpoints.ExtensionEndpoints;
import dev.kiket.sdk.secrets.ExtensionSecretManager;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Context passed to webhook handlers.
 */
@Data
@Builder
public class HandlerContext {
    private String event;
    private String eventVersion;
    private Map<String, String> headers;
    private KiketClient client;
    private ExtensionEndpoints endpoints;
    private Map<String, Object> settings;
    private String extensionId;
    private String extensionVersion;
    private ExtensionSecretManager secrets;
}
