package org.example.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Class that reads the config.properties file and provides the values to the application.
 */
public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Unable to find config.properties");
            }

            // Load a properties file from class path
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getServerAddress() {
        return properties.getProperty("server.address");
    }

    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port"));
    }
}
