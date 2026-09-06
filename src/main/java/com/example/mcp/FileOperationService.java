package com.example.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service that performs file operations within a sandboxed base directory.
 * This is called directly by the MCP controller (not invoked via LLM chat).
 */
@Service
public class FileOperationService {

    private static final Logger log = LoggerFactory.getLogger(FileOperationService.class);

    private final Path basePath;

    public FileOperationService() {
        // Use a sandboxed directory for safety
        this.basePath = Paths.get("data/sandbox");
    }

    /**
     * Constructor for testing with a custom base directory.
     */
    public FileOperationService(Path basePath) {
        this.basePath = basePath;
    }

    /**
     * Reads a file from the sandbox directory.
     */
    public String readFile(String path) {
        Path filePath = basePath.resolve(path).normalize();

        // Security: ensure the resolved path is still under the base directory
        if (!filePath.startsWith(basePath)) {
            log.warn("Access denied: attempt to read file outside sandbox: {}", path);
            throw new IllegalArgumentException(
                "Access denied: path must be within base directory (" + basePath + ")"
            );
        }

        log.debug("Reading file: {}", filePath);
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            log.info("File read successfully: {} ({} bytes)", filePath, content.length());
            return content;
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            throw new IllegalStateException("Failed to read file: " + path, e);
        }
    }

    /**
     * Writes content to a file in the sandbox directory.
     */
    public void writeFile(String path, String content) {
        Path filePath = basePath.resolve(path).normalize();

        // Security: ensure the resolved path is still under the base directory
        if (!filePath.startsWith(basePath)) {
            log.warn("Access denied: attempt to write file outside sandbox: {}", path);
            throw new IllegalArgumentException(
                "Access denied: path must be within base directory (" + basePath + ")"
            );
        }

        log.debug("Writing file: {} ({} bytes)", filePath, content != null ? content.length() : 0);
        try {
            // Create parent directories if they don't exist
            Files.createDirectories(filePath.getParent());
            
            // Handle null content by writing empty string
            String contentToWrite = content != null ? content : "";
            Files.writeString(filePath, contentToWrite, StandardCharsets.UTF_8);
            log.info("File written successfully: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to write file: {}", filePath, e);
            throw new IllegalStateException("Failed to write file: " + path, e);
        }
    }

    public Path getBasePath() {
        return basePath;
    }
}