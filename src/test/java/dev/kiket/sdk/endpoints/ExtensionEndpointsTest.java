package dev.kiket.sdk.endpoints;

import dev.kiket.sdk.client.KiketClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExtensionEndpointsTest {

    @Test
    void rateLimitReturnsPayload() {
        KiketClient client = Mockito.mock(KiketClient.class);
        RateLimitInfo info = new RateLimitInfo();
        info.setLimit(600);
        info.setRemaining(42);
        info.setWindowSeconds(60);
        info.setResetIn(12);

        RateLimitResponse response = new RateLimitResponse();
        response.setRateLimit(info);

        Mockito.when(client.get(Mockito.eq("/api/v1/ext/rate_limit"), Mockito.eq(RateLimitResponse.class)))
            .thenReturn(Mono.just(response));

        ExtensionEndpoints endpoints = new ExtensionEndpoints(client, "ext-1", "v1");
        RateLimitInfo result = endpoints.rateLimit();

        assertNotNull(result);
        assertEquals(600, result.getLimit());
        assertEquals(42, result.getRemaining());
    }
}
