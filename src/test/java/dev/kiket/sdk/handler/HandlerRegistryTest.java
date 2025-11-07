package dev.kiket.sdk.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandlerRegistryTest {

    private HandlerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new HandlerRegistry();
    }

    @Test
    void testRegisterHandler() {
        WebhookHandler handler = (payload, context) -> null;

        registry.register("test.event", "v1", handler);

        HandlerRegistry.HandlerMetadata metadata = registry.get("test.event", "v1");
        assertNotNull(metadata);
        assertEquals("test.event", metadata.getEvent());
        assertEquals("v1", metadata.getVersion());
        assertEquals(handler, metadata.getHandler());
    }

    @Test
    void testRegisterMultipleVersions() {
        WebhookHandler handlerV1 = (payload, context) -> "v1";
        WebhookHandler handlerV2 = (payload, context) -> "v2";

        registry.register("test.event", "v1", handlerV1);
        registry.register("test.event", "v2", handlerV2);

        assertEquals(handlerV1, registry.get("test.event", "v1").getHandler());
        assertEquals(handlerV2, registry.get("test.event", "v2").getHandler());
    }

    @Test
    void testGetUnregisteredHandler() {
        HandlerRegistry.HandlerMetadata metadata = registry.get("unknown.event", "v1");

        assertNull(metadata);
    }

    @Test
    void testEventNames() {
        registry.register("event1", "v1", (payload, context) -> null);
        registry.register("event1", "v2", (payload, context) -> null);
        registry.register("event2", "v1", (payload, context) -> null);

        List<String> eventNames = registry.eventNames();

        assertEquals(2, eventNames.size());
        assertTrue(eventNames.contains("event1"));
        assertTrue(eventNames.contains("event2"));
    }

    @Test
    void testEventNamesEmpty() {
        List<String> eventNames = registry.eventNames();

        assertTrue(eventNames.isEmpty());
    }

    @Test
    void testAllHandlers() {
        registry.register("event1", "v1", (payload, context) -> null);
        registry.register("event2", "v1", (payload, context) -> null);

        assertEquals(2, registry.all().size());
    }

    @Test
    void testOverwriteHandler() {
        WebhookHandler handler1 = (payload, context) -> "first";
        WebhookHandler handler2 = (payload, context) -> "second";

        registry.register("test.event", "v1", handler1);
        registry.register("test.event", "v1", handler2);

        assertEquals(handler2, registry.get("test.event", "v1").getHandler());
    }
}
