package dev.kiket.sdk.config;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extension manifest structure.
 */
@Data
public class ExtensionManifest {
    private String id;
    private String version;
    private String deliverySecret;
    private List<Setting> settings;

    public static ExtensionManifest fromMap(Map<String, Object> data) {
        ExtensionManifest manifest = new ExtensionManifest();
        manifest.setId((String) data.get("id"));
        manifest.setVersion((String) data.get("version"));
        manifest.setDeliverySecret((String) data.get("delivery_secret"));

        if (data.get("settings") instanceof List) {
            List<Map<String, Object>> settingsList = (List<Map<String, Object>>) data.get("settings");
            manifest.setSettings(settingsList.stream()
                .map(Setting::fromMap)
                .collect(Collectors.toList()));
        }

        return manifest;
    }

    public Map<String, Object> getSettingsDefaults() {
        if (settings == null) {
            return new HashMap<>();
        }

        Map<String, Object> defaults = new HashMap<>();
        for (Setting setting : settings) {
            if (setting.getDefaultValue() != null) {
                defaults.put(setting.getKey(), setting.getDefaultValue());
            }
        }
        return defaults;
    }

    public List<String> getSecretKeys() {
        if (settings == null) {
            return new ArrayList<>();
        }

        return settings.stream()
            .filter(Setting::isSecret)
            .map(Setting::getKey)
            .collect(Collectors.toList());
    }

    public Map<String, Object> applySecretEnvOverrides() {
        Map<String, Object> overrides = new HashMap<>();
        for (String key : getSecretKeys()) {
            String envKey = "KIKET_SECRET_" + key.toUpperCase();
            // Check system property first (for testing), then environment variable
            String envValue = System.getProperty(envKey);
            if (envValue == null) {
                envValue = System.getenv(envKey);
            }
            if (envValue != null) {
                overrides.put(key, envValue);
            }
        }
        return overrides;
    }

    @Data
    public static class Setting {
        private String key;
        private Object defaultValue;
        private boolean secret;

        public static Setting fromMap(Map<String, Object> data) {
            Setting setting = new Setting();
            setting.setKey((String) data.get("key"));
            setting.setDefaultValue(data.get("default"));
            setting.setSecret(Boolean.TRUE.equals(data.get("secret")));
            return setting;
        }
    }
}
