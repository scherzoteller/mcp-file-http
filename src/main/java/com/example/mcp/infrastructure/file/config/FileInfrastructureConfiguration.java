package com.example.mcp.infrastructure.file.config;

import com.example.mcp.application.file.config.ApplicationConfiguration;
import com.example.mcp.domain.file.ports.FilePort;
import com.example.mcp.infrastructure.file.adapter.FileSystemFileAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

/**
 * Configuration class for file infrastructure beans.
 * Provides the FilePort bean (infrastructure adapter) with configuration from McpFileProperties.
 */
@Configuration
public class FileInfrastructureConfiguration {

    /**
     * Creates the FilePort bean (infrastructure adapter).
     *
     * @param properties the MCP file properties configuration
     * @return the FilePort implementation
     */
    @Bean
    public FilePort filePort(ApplicationConfiguration.McpFileProperties properties) {
        String baseDir = properties.getBaseDir();
        if (baseDir != null && !baseDir.isEmpty()) {
            return new FileSystemFileAdapter(Paths.get(baseDir));
        }
        return new FileSystemFileAdapter();
    }
}
