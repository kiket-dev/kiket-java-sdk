package dev.kiket.sdk.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Loader for extension manifest files.
 */
public class ManifestLoader {

    public static ExtensionManifest load(String manifestPath) {
        String[] paths = manifestPath != null
            ? new String[]{manifestPath}
            : new String[]{"extension.yaml", "manifest.yaml", "extension.yml", "manifest.yml"};

        for (String path : paths) {
            Path fullPath = Paths.get(System.getProperty("user.dir"), path);
            if (Files.exists(fullPath)) {
                try (InputStream inputStream = new FileInputStream(fullPath.toFile())) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(inputStream);
                    return ExtensionManifest.fromMap(data);
                } catch (IOException e) {
                    System.err.println("Failed to parse manifest at " + fullPath + ": " + e.getMessage());
                }
            }
        }

        return null;
    }
}
