# Kiket Java SDK

> Build and run Kiket extensions with a batteries-included, strongly-typed Java toolkit.

## Features

- 🔌 **Webhook handlers** – register handlers for events with `sdk.register("issue.created", "v1", handler)`.
- 🔐 **Transparent authentication** – HMAC verification for inbound payloads, workspace-token client for outbound calls.
- 🔑 **Secret manager** – list, fetch, rotate, and delete extension secrets stored in Google Secret Manager.
- 🌐 **Built-in Spring Boot app** – serve extension webhooks locally or in production without extra wiring.
- 🔁 **Version-aware routing** – register multiple handlers per event and propagate version headers on outbound calls.
- 📦 **Manifest-aware defaults** – automatically loads `extension.yaml`/`manifest.yaml`, applies configuration defaults, and hydrates secrets from `KIKET_SECRET_*` environment variables.
- 🧱 **Typed & documented** – designed for Java 17+ with full type safety and rich Javadoc comments.
- 📊 **Telemetry & feedback hooks** – capture handler duration/success metrics automatically.
- 📇 **Custom data client** – call `/api/v1/ext/custom_data/...` with `context.getEndpoints().customData(projectId)` using the runtime token.
- 📉 **Rate-limit helper** – inspect `/api/v1/ext/rate_limit` via `context.getEndpoints().rateLimit()` before fanning out jobs.

## Quickstart

### Maven

```xml
<dependency>
  <groupId>dev.kiket</groupId>
  <artifactId>kiket-sdk</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Example

```java
import dev.kiket.sdk.KiketSDK;
import dev.kiket.sdk.handler.WebhookHandler;

public class Main {
    public static void main(String[] args) {
        KiketSDK sdk = KiketSDK.builder()
            .webhookSecret("sh_123")
            .workspaceToken("wk_test")
            .extensionId("com.example.marketing")
            .extensionVersion("1.0.0")
            .build();

        // Register webhook handler (v1)
        sdk.register("issue.created", "v1", (payload, context) -> {
            String summary = (String) ((Map) payload.get("issue")).get("title");
            System.out.println("Event version: " + context.getEventVersion());

            context.getEndpoints().logEvent("issue.created", Map.of("summary", summary));
            context.getSecrets().set("WEBHOOK_TOKEN", "abc123");

            return Map.of("ok", true);
        });

        // Register webhook handler (v2)
        sdk.register("issue.created", "v2", (payload, context) -> {
            String summary = (String) ((Map) payload.get("issue")).get("title");

            context.getEndpoints().logEvent("issue.created", Map.of(
                "summary", summary,
                "schema", "v2"
            ));

            return Map.of("ok", true, "version", context.getEventVersion());
        });

        sdk.run("0.0.0.0", 8080);
    }
}
```

### Custom Data Client

When your manifest defines `custom_data.permissions`, the SDK automatically uses the runtime token provided in the webhook payload for API calls via `context.getClient()`:

```java
sdk.register("issue.created", "v1", (payload, context) -> {
    String projectId = String.valueOf(((Map<?, ?>) payload.get("issue")).get("project_id"));
    var customData = context.getEndpoints().customData(projectId);

    var options = new CustomDataClient.CustomDataListOptions();
    options.setLimit(10);
    options.setFilters(Map.of("status", "active"));
    customData.list("com.example.crm.contacts", "automation_records", options);

    customData.create("com.example.crm.contacts", "automation_records", Map.of(
        "email", "lead@example.com",
        "metadata", Map.of("source", "webhook")
    ));

    return Map.of("ok", true);
});
```

### SLA Alert Stream

SLA monitors raise `workflow.sla_status` events. Use the helper to inspect current alerts:

```java
sdk.register("workflow.sla_status", "v1", (payload, context) -> {
    String projectId = String.valueOf(((Map<?, ?>) payload.get("issue")).get("project_id"));
    var slaClient = context.getEndpoints().slaEvents(projectId);

    var options = new SlaEventsClient.SlaEventsListOptions();
    options.setState("imminent");
    options.setLimit(5);

    var events = slaClient.list(options);
    if (events.getData().isEmpty()) {
        return Map.of("ok", true);
    }

    var first = events.getData().get(0);
    context.getEndpoints().logEvent("sla.warning", Map.of(
        "issue_id", first.get("issue_id"),
        "state", first.get("state")
    ));

    return Map.of("acknowledged", true);
});
```

## Configuration

### Environment Variables

- `KIKET_WEBHOOK_SECRET` – Webhook HMAC secret for signature verification
- `KIKET_WORKSPACE_TOKEN` – Workspace token for API authentication
- `KIKET_BASE_URL` – Kiket API base URL (defaults to `https://kiket.dev`)
- `KIKET_SDK_TELEMETRY_URL` – Telemetry reporting endpoint (optional)
- `KIKET_SDK_TELEMETRY_OPTOUT` – Set to `1` to disable telemetry
- `KIKET_SECRET_*` – Secret overrides (e.g., `KIKET_SECRET_API_KEY`)

### Manifest File

Create an `extension.yaml` or `manifest.yaml` file:

```yaml
id: com.example.marketing
version: 1.0.0
delivery_secret: sh_production_secret

settings:
  - key: API_KEY
    secret: true
  - key: MAX_RETRIES
    default: 3
  - key: TIMEOUT_MS
    default: 5000
```

## API Reference

### KiketSDK

Main SDK class for building extensions.

```java
KiketSDK sdk = KiketSDK.builder()
    .webhookSecret(String)
    .workspaceToken(String)
    .baseUrl(String)
    .settings(Map<String, Object>)
    .extensionId(String)
    .extensionVersion(String)
    .manifestPath(String)
    .autoEnvSecrets(boolean)
    .telemetryEnabled(boolean)
    .feedbackHook(FeedbackHook)
    .telemetryUrl(String)
    .build();
```

**Methods:**

- `sdk.register(String event, String version, WebhookHandler handler)` – Register a webhook handler
- `sdk.run(String host, int port)` – Start the Spring Boot server
- `sdk.stop()` – Stop the server

### HandlerContext

Context passed to webhook handlers:

```java
public interface HandlerContext {
    String getEvent();
    String getEventVersion();
    Map<String, String> getHeaders();
    KiketClient getClient();
    ExtensionEndpoints getEndpoints();
    Map<String, Object> getSettings();
    String getExtensionId();
    String getExtensionVersion();
    ExtensionSecretManager getSecrets();
    String secret(String key);  // Secret helper with payload-first fallback
}
```

### Response Helpers

Use the `ExtensionResponse` class to build properly formatted responses:

```java
import dev.kiket.sdk.responses.ExtensionResponse;

// Simple allow
return ExtensionResponse.allow().build();

// Allow with message and data
return ExtensionResponse.allow()
    .message("Successfully configured")
    .data("routeId", 123)
    .build();

// Allow with output fields (displayed in configuration UI)
return ExtensionResponse.allow()
    .message("Mailjet configured successfully")
    .data("routeId", route.getId())
    .outputField("inbound_email", route.getEmail())
    .build();

// Deny with error details
return ExtensionResponse.deny("Invalid credentials")
    .data("errorCode", "AUTH_FAILED")
    .build();

// Pending for async operations
return ExtensionResponse.pending("Awaiting approval")
    .data("jobId", "abc123")
    .build();
```

Output fields are displayed in the extension configuration UI after setup, allowing extensions to expose generated data like email addresses, webhook URLs, or status information.

### Secret Helper

The `secret()` method provides a simple way to retrieve secrets with automatic fallback:

```java
// Checks payload secrets first (per-org config), falls back to ENV
String slackToken = context.secret("SLACK_BOT_TOKEN");

// Example usage
sdk.register("issue.created", "v1", (payload, context) -> {
    String apiKey = context.secret("API_KEY");
    if (apiKey == null) {
        throw new IllegalStateException("API_KEY not configured");
    }
    // Use apiKey...
    return Map.of("ok", true);
});
```

The lookup order is:
1. **Payload secrets** (per-org configuration from `payload["secrets"]`)
2. **Environment variables** (extension defaults via `System.getenv()`)

This allows organizations to override extension defaults with their own credentials.

## Publishing to GitHub Packages

When you are ready to cut a release:

1. Update the version in `pom.xml`.
2. Run the test suite (`mvn test`).
3. Build distributables:
   ```bash
   mvn clean package
   ```
4. Commit and tag the release:
   ```bash
   git add pom.xml
   git commit -m "Bump Java SDK to v0.x.y"
   git tag java-v0.x.y
   git push --tags
   ```
5. GitHub Actions will automatically publish to GitHub Packages.

## License

MIT
### Rate-Limit Helper

Throttle expensive webhooks by checking the remaining window:

```java
sdk.register("automation.dispatch", "v1", (payload, context) -> {
    RateLimitInfo limits = context.getEndpoints().rateLimit();
    if (limits != null && limits.getRemaining() < 5) {
        context.getEndpoints().logEvent("rate_limited", Map.of(
            "remaining", limits.getRemaining(),
            "reset_in", limits.getResetIn()
        ));
        return Map.of("deferred", true);
    }

    // Continue with heavy work
    return Map.of("ok", true);
});
```
