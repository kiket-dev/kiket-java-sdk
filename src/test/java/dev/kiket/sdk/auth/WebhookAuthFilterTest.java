package dev.kiket.sdk.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class WebhookAuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private WebhookAuthFilter filter;
    private final String secret = "test-secret";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new WebhookAuthFilter(secret);
    }

    @Test
    void testValidSignature() throws Exception {
        String body = "{\"test\":\"data\"}";
        long timestamp = System.currentTimeMillis() / 1000;
        String signature = generateSignature(secret, body, String.valueOf(timestamp));

        when(request.getRequestURI()).thenReturn("/webhooks/test.event");
        when(request.getHeader("X-Kiket-Signature")).thenReturn(signature);
        when(request.getHeader("X-Kiket-Timestamp")).thenReturn(String.valueOf(timestamp));
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testMissingSignatureHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/webhooks/test.event");
        when(request.getHeader("X-Kiket-Signature")).thenReturn(null);
        when(request.getHeader("X-Kiket-Timestamp")).thenReturn("123456789");

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testMissingTimestampHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/webhooks/test.event");
        when(request.getHeader("X-Kiket-Signature")).thenReturn("abc123");
        when(request.getHeader("X-Kiket-Timestamp")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testInvalidSignature() throws Exception {
        String body = "{\"test\":\"data\"}";
        long timestamp = System.currentTimeMillis() / 1000;

        when(request.getRequestURI()).thenReturn("/webhooks/test.event");
        when(request.getHeader("X-Kiket-Signature")).thenReturn("invalid-signature");
        when(request.getHeader("X-Kiket-Timestamp")).thenReturn(String.valueOf(timestamp));
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testExpiredTimestamp() throws Exception {
        String body = "{\"test\":\"data\"}";
        long timestamp = (System.currentTimeMillis() / 1000) - 400; // 400 seconds ago
        String signature = generateSignature(secret, body, String.valueOf(timestamp));

        when(request.getRequestURI()).thenReturn("/webhooks/test.event");
        when(request.getHeader("X-Kiket-Signature")).thenReturn(signature);
        when(request.getHeader("X-Kiket-Timestamp")).thenReturn(String.valueOf(timestamp));
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testNonWebhookEndpoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/health");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    private String generateSignature(String key, String body, String timestamp) throws Exception {
        String payload = timestamp + "." + body;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmac = mac.doFinal(payload.getBytes());

        StringBuilder result = new StringBuilder();
        for (byte b : hmac) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
