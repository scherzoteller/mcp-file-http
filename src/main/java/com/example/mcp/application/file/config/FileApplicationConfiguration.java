package com.example.mcp.application.file.config;

import com.example.mcp.application.file.services.FileOperationAppService;
import com.example.mcp.domain.file.ports.FilePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for file application-layer beans.
 * Provides the FileOperationAppService bean with the injected FilePort dependency.
 */
@Configuration
public class FileApplicationConfiguration {

    /**
     * Creates the FileOperationAppService bean (application service).
     *
     * @param filePort the FilePort implementation from infrastructure layer
     * @return the FileOperationAppService instance
     */
    @Bean
    public FileOperationAppService fileOperationAppService(FilePort filePort) {
        return new FileOperationAppService(filePort);
    }
}