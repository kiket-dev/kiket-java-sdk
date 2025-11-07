package dev.kiket.sdk.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionManifestTest {

    @Test
    void testFromMap() {
        Map<String, Object> data = Map.of(
            "id", "com.example.test",
            "version", "1.0.0",
            "delivery_secret", "secret123",
            "settings", List.of(
                Map.of("key", "API_KEY", "secret", true),
                Map.of("key", "MAX_RETRIES", "default", 3)
            )
        );

        ExtensionManifest manifest = ExtensionManifest.fromMap(data);

        assertEquals("com.example.test", manifest.getId());
        assertEquals("1.0.0", manifest.getVersion());
        assertEquals("secret123", manifest.getDeliverySecret());
        assertEquals(2, manifest.getSettings().size());
    }

    @Test
    void testGetSettingsDefaults() {
        ExtensionManifest manifest = new ExtensionManifest();
        ExtensionManifest.Setting setting1 = new ExtensionManifest.Setting();
        setting1.setKey("API_KEY");
        setting1.setSecret(true);

        ExtensionManifest.Setting setting2 = new ExtensionManifest.Setting();
        setting2.setKey("MAX_RETRIES");
        setting2.setDefaultValue(3);

        manifest.setSettings(List.of(setting1, setting2));

        Map<String, Object> defaults = manifest.getSettingsDefaults();

        assertEquals(1, defaults.size());
        assertEquals(3, defaults.get("MAX_RETRIES"));
        assertFalse(defaults.containsKey("API_KEY"));
    }

    @Test
    void testGetSettingsDefaultsEmpty() {
        ExtensionManifest manifest = new ExtensionManifest();

        Map<String, Object> defaults = manifest.getSettingsDefaults();

        assertTrue(defaults.isEmpty());
    }

    @Test
    void testGetSecretKeys() {
        ExtensionManifest manifest = new ExtensionManifest();
        ExtensionManifest.Setting setting1 = new ExtensionManifest.Setting();
        setting1.setKey("API_KEY");
        setting1.setSecret(true);

        ExtensionManifest.Setting setting2 = new ExtensionManifest.Setting();
        setting2.setKey("SECRET_TOKEN");
        setting2.setSecret(true);

        ExtensionManifest.Setting setting3 = new ExtensionManifest.Setting();
        setting3.setKey("MAX_RETRIES");
        setting3.setSecret(false);

        manifest.setSettings(List.of(setting1, setting2, setting3));

        List<String> secretKeys = manifest.getSecretKeys();

        assertEquals(2, secretKeys.size());
        assertTrue(secretKeys.contains("API_KEY"));
        assertTrue(secretKeys.contains("SECRET_TOKEN"));
        assertFalse(secretKeys.contains("MAX_RETRIES"));
    }

    @Test
    void testApplySecretEnvOverrides() {
        System.setProperty("KIKET_SECRET_API_KEY", "env-value");

        ExtensionManifest manifest = new ExtensionManifest();
        ExtensionManifest.Setting setting = new ExtensionManifest.Setting();
        setting.setKey("API_KEY");
        setting.setSecret(true);
        manifest.setSettings(List.of(setting));

        Map<String, Object> overrides = manifest.applySecretEnvOverrides();

        assertEquals(1, overrides.size());
        assertEquals("env-value", overrides.get("API_KEY"));

        System.clearProperty("KIKET_SECRET_API_KEY");
    }
}
