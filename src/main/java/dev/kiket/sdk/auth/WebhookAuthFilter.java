package dev.kiket.sdk.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Filter for verifying webhook HMAC signatures.
 */
@Component
public class WebhookAuthFilter implements Filter {

    private final String secret;

    public WebhookAuthFilter(String secret) {
        this.secret = secret;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Only verify webhook endpoints
        String path = httpRequest.getRequestURI();
        if (!path.contains("/webhooks/")) {
            chain.doFilter(request, response);
            return;
        }

        String signature = httpRequest.getHeader("X-Kiket-Signature");
        String timestamp = httpRequest.getHeader("X-Kiket-Timestamp");

        if (signature == null || timestamp == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\": \"Missing signature headers\"}");
            return;
        }

        // Verify timestamp (5 minute window)
        long now = System.currentTimeMillis() / 1000;
        long requestTime;
        try {
            requestTime = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\": \"Invalid timestamp\"}");
            return;
        }

        long timeDiff = Math.abs(now - requestTime);
        if (timeDiff > 300) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\": \"Request timestamp too old\"}");
            return;
        }

        // Read body and verify signature
        String body = httpRequest.getReader().lines()
            .reduce("", (accumulator, actual) -> accumulator + actual);

        String payload = timestamp + "." + body;
        String expectedSignature;
        try {
            expectedSignature = computeHmacSha256(secret, payload);
        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.getWriter().write("{\"error\": \"Signature verification failed\"}");
            return;
        }

        if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8),
                                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\": \"Invalid signature\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String computeHmacSha256(String key, String data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmac) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
