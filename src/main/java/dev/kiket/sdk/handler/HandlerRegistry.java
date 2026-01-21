package dev.kiket.sdk.handler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for webhook handlers.
 */
public class HandlerRegistry {
    private final Map<String, HandlerMetadata> handlers = new ConcurrentHashMap<>();

    /**
     * Register a webhook handler.
     */
    public void register(String event, String version, WebhookHandler handler) {
        String key = makeKey(event, version);
        handlers.put(key, new HandlerMetadata(event, version, handler));
    }

    /**
     * Get a handler for an event and version.
     */
    public HandlerMetadata get(String event, String version) {
        String key = makeKey(event, version);
        return handlers.get(key);
    }

    /**
     * Get all registered event names.
     */
    public List<String> eventNames() {
        Set<String> events = new HashSet<>();
        for (HandlerMetadata metadata : handlers.values()) {
            events.add(metadata.getEvent());
        }
        return new ArrayList<>(events);
    }

    /**
     * Get all handlers.
     */
    public Collection<HandlerMetadata> all() {
        return handlers.values();
    }

    private String makeKey(String event, String version) {
        return event + ":" + version;
    }

    public static class HandlerMetadata {
        private final String event;
        private final String version;
        private final WebhookHandler handler;

        public HandlerMetadata(String event, String version, WebhookHandler handler) {
            this.event = event;
            this.version = version;
            this.handler = handler;
        }

        public String getEvent() { return event; }
        public String getVersion() { return version; }
        public WebhookHandler getHandler() { return handler; }
    }
}
