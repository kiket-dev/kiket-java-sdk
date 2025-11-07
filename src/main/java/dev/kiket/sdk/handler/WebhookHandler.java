package dev.kiket.sdk.handler;

import java.util.Map;

/**
 * Functional interface for webhook handlers.
 */
@FunctionalInterface
public interface WebhookHandler {
    /**
     * Handle a webhook event.
     *
     * @param payload Event payload
     * @param context Handler context
     * @return Response object
     * @throws Exception if handler fails
     */
    Object handle(Map<String, Object> payload, HandlerContext context) throws Exception;
}
