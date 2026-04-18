package com.piedrazul.frontend.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lee application.properties del classpath.
 * Reemplaza @Value / Environment de Spring.
 */
public class AppConfig {

    private final Properties props = new Properties();

    public AppConfig() {
        try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer application.properties", e);
        }
    }

    public String getGatewayUrl() {
        return props.getProperty("gateway.url", "http://localhost:8080");
    }

    public int getConnectTimeout() {
        return Integer.parseInt(props.getProperty("http.connect.timeout", "5000"));
    }

    public int getReadTimeout() {
        return Integer.parseInt(props.getProperty("http.read.timeout", "10000"));
    }
}
