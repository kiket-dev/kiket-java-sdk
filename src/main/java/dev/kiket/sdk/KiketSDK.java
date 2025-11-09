package dev.kiket.sdk;

import dev.kiket.sdk.auth.WebhookAuthFilter;
import dev.kiket.sdk.config.SDKConfig;
import dev.kiket.sdk.config.ExtensionManifest;
import dev.kiket.sdk.config.ManifestLoader;
import dev.kiket.sdk.handler.HandlerRegistry;
import dev.kiket.sdk.handler.WebhookHandler;
import dev.kiket.sdk.telemetry.TelemetryReporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Main SDK class for building Kiket extensions.
 */
@SpringBootApplication
@Configuration
public class KiketSDK {

    private final SDKConfig config;
    private final HandlerRegistry registry;
    private final TelemetryReporter telemetry;
    private final ExtensionManifest manifest;
    private ConfigurableApplicationContext context;

    /**
     * Create a new KiketSDK instance.
     *
     * @param builder Configuration builder
     */
    public KiketSDK(Builder builder) {
        this.manifest = ManifestLoader.load(builder.manifestPath);
        this.config = resolveConfig(builder, manifest);
        this.registry = new HandlerRegistry();
        this.telemetry = new TelemetryReporter(
            config.isTelemetryEnabled(),
            config.getTelemetryUrl(),
            config.getFeedbackHook(),
            config.getExtensionId(),
            config.getExtensionVersion(),
            config.getExtensionApiKey()
        );
    }

    /**
     * Register a webhook handler.
     *
     * @param event Event name
     * @param version Event version
     * @param handler Handler function
     */
    public void register(String event, String version, WebhookHandler handler) {
        registry.register(event, version, handler);
    }

    /**
     * Start the Spring Boot application.
     *
     * @param host Host to bind to
     * @param port Port to bind to
     */
    public void run(String host, int port) {
        SpringApplication app = new SpringApplication(KiketSDK.class);
        app.setDefaultProperties(Map.of(
            "server.address", host,
            "server.port", String.valueOf(port)
        ));
        this.context = app.run();

        System.out.println("🚀 Kiket extension listening on http://" + host + ":" + port);
        System.out.println("📦 Extension: " + (config.getExtensionId() != null ? config.getExtensionId() : "unknown"));
        System.out.println("📝 Registered events: " + String.join(", ", registry.eventNames()));
    }

    /**
     * Stop the Spring Boot application.
     */
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    @Bean
    public SDKConfig sdkConfig() {
        return config;
    }

    @Bean
    public HandlerRegistry handlerRegistry() {
        return registry;
    }

    @Bean
    public TelemetryReporter telemetryReporter() {
        return telemetry;
    }

    @Bean
    public WebhookAuthFilter webhookAuthFilter() {
        return new WebhookAuthFilter(config.getWebhookSecret());
    }

    private SDKConfig resolveConfig(Builder builder, ExtensionManifest manifest) {
        String baseUrl = builder.baseUrl != null ? builder.baseUrl
            : System.getenv("KIKET_BASE_URL") != null ? System.getenv("KIKET_BASE_URL")
            : "https://kiket.dev";

        String workspaceToken = builder.workspaceToken != null ? builder.workspaceToken
            : System.getenv("KIKET_WORKSPACE_TOKEN");

        String webhookSecret = builder.webhookSecret != null ? builder.webhookSecret
            : manifest != null && manifest.getDeliverySecret() != null ? manifest.getDeliverySecret()
            : System.getenv("KIKET_WEBHOOK_SECRET");

        Map<String, Object> settings = new HashMap<>();
        if (manifest != null) {
            settings.putAll(manifest.getSettingsDefaults());
            if (builder.autoEnvSecrets) {
                settings.putAll(manifest.applySecretEnvOverrides());
            }
        }
        if (builder.settings != null) {
            settings.putAll(builder.settings);
        }

        String extensionId = builder.extensionId != null ? builder.extensionId
            : manifest != null ? manifest.getId() : null;

        String extensionVersion = builder.extensionVersion != null ? builder.extensionVersion
            : manifest != null ? manifest.getVersion() : null;

        String telemetryUrl = builder.telemetryUrl != null ? builder.telemetryUrl
            : System.getenv("KIKET_SDK_TELEMETRY_URL") != null ? System.getenv("KIKET_SDK_TELEMETRY_URL")
            : baseUrl.replaceAll("/+$", "") + "/api/v1/ext";

        String extensionApiKey = builder.extensionApiKey != null ? builder.extensionApiKey
            : System.getenv("KIKET_EXTENSION_API_KEY");

        return SDKConfig.builder()
            .webhookSecret(webhookSecret)
            .workspaceToken(workspaceToken)
            .baseUrl(baseUrl)
            .settings(settings)
            .extensionId(extensionId)
            .extensionVersion(extensionVersion)
            .telemetryEnabled(builder.telemetryEnabled)
            .feedbackHook(builder.feedbackHook)
            .telemetryUrl(telemetryUrl)
            .extensionApiKey(extensionApiKey)
            .build();
    }

    /**
     * Create a new builder for KiketSDK.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for KiketSDK configuration.
     */
    public static class Builder {
        private String webhookSecret;
        private String workspaceToken;
        private String baseUrl;
        private Map<String, Object> settings;
        private String extensionId;
        private String extensionVersion;
        private String manifestPath;
        private boolean autoEnvSecrets = true;
        private boolean telemetryEnabled = true;
        private TelemetryReporter.FeedbackHook feedbackHook;
        private String telemetryUrl;
        private String extensionApiKey;

        public Builder webhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
            return this;
        }

        public Builder workspaceToken(String workspaceToken) {
            this.workspaceToken = workspaceToken;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder settings(Map<String, Object> settings) {
            this.settings = settings;
            return this;
        }

        public Builder extensionId(String extensionId) {
            this.extensionId = extensionId;
            return this;
        }

        public Builder extensionVersion(String extensionVersion) {
            this.extensionVersion = extensionVersion;
            return this;
        }

        public Builder manifestPath(String manifestPath) {
            this.manifestPath = manifestPath;
            return this;
        }

        public Builder autoEnvSecrets(boolean autoEnvSecrets) {
            this.autoEnvSecrets = autoEnvSecrets;
            return this;
        }

        public Builder telemetryEnabled(boolean telemetryEnabled) {
            this.telemetryEnabled = telemetryEnabled;
            return this;
        }

        public Builder feedbackHook(TelemetryReporter.FeedbackHook feedbackHook) {
            this.feedbackHook = feedbackHook;
            return this;
        }

        public Builder telemetryUrl(String telemetryUrl) {
            this.telemetryUrl = telemetryUrl;
            return this;
        }

        public Builder extensionApiKey(String extensionApiKey) {
            this.extensionApiKey = extensionApiKey;
            return this;
        }

        public KiketSDK build() {
            return new KiketSDK(this);
        }
    }
}
