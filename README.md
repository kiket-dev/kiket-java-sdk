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
}
```

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
