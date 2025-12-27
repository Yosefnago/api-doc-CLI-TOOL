package com.dev.tools.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Stores and retrieves the ApiDoc project root path.
 *
 * <p>
 * The configuration is persisted in a {@code config.properties} file located
 * in the CLI directory defined by the {@code SCRIPT_DIR} environment variable.
 * </p>
 *
 * <p>
 * The key {@code root.path} holds the project root used by the {@code generate} command
 * unless overridden on the command line.
 * </p>
 *
 * @author Yosef Nago
 * @since 1.1.0
 */
public class PathConfig {

    private static final Path CONFIG_FILE =
            Path.of(System.getenv("SCRIPT_DIR"), "config.properties");

    private static final String KEY = "root.path";

    public static String getPath() {
        Properties p = load();
        return p.getProperty(KEY, "src/main/java");
    }

    public static void setPath(String path) {
        Properties p = load();
        p.setProperty(KEY, path);
        save(p);
    }

    private static Properties load() {
        try {
            if (Files.notExists(CONFIG_FILE)) {
                Files.createDirectories(CONFIG_FILE.getParent());
                Files.createFile(CONFIG_FILE);
            }
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
                p.load(in);
            }
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config", e);
        }
    }

    private static void save(Properties p) {
        try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
            p.store(out, "ApiDoc configuration");
        } catch (IOException e) {
            throw new RuntimeException("Cannot save config", e);
        }
    }
}