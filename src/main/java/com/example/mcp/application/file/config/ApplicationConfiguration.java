package com.example.mcp.application.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for application-layer properties.
 * McpFileProperties is defined here so infrastructure can depend on the application layer
 * rather than the exposition layer, respecting hexagonal architecture dependencies.
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * Creates the McpFileProperties bean for MCP file configuration.
     *
     * @return the MCP file properties
     */
    @Bean
    @ConfigurationProperties(prefix = "mcp")
    public McpFileProperties mcpFileProperties() {
        return new McpFileProperties();
    }

    /**
     * Properties class for MCP file configuration.
     */
    public static class McpFileProperties {
        private String baseDir;

        public String getBaseDir() {
            return baseDir;
        }

        public void setBaseDir(String baseDir) {
            this.baseDir = baseDir;
        }
    }
}