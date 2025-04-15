package edu.upc.essi.dtim.nextiamg.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    // Load configuration from a file
    public static void loadProperties(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration file: " + filePath, e);
        }
    }

    // Get property value or return a default if missing
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static Iterable<String> getAllKeys() {
        return properties.stringPropertyNames();
    }

}

