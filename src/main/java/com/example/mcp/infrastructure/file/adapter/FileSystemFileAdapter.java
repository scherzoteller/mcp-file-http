package com.example.mcp.infrastructure.file.adapter;

import com.example.mcp.domain.file.ports.FilePort;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Infrastructure adapter implementing FilePort for file system operations.
 * Provides the technical implementation for file access using the Java NFS.
 */
public class FileSystemFileAdapter implements FilePort {

    private final Path basePath;

    /**
     * Creates a FileSystemFileAdapter with the default sandbox directory.
     */
    public FileSystemFileAdapter() {
        this.basePath = Paths.get("data/sandbox");
    }

    /**
     * Creates a FileSystemFileAdapter with a custom base directory.
     *
     * @param basePath the base directory for file operations
     */
    public FileSystemFileAdapter(Path basePath) {
        this.basePath = Objects.requireNonNull(basePath, "basePath cannot be null");
    }

    @Override
    public String readFile(String path) {
        Path filePath = resolveAndValidatePath(path);

        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file: " + path, e);
        }
    }

    @Override
    public void writeFile(String path, String content) {
        Path filePath = resolveAndValidatePath(path);

        try {
            Files.createDirectories(filePath.getParent());
            String contentToWrite = content != null ? content : "";
            Files.writeString(filePath, contentToWrite, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write file: " + path, e);
        }
    }

    /**
     * Resolves and validates the path to ensure it's within the base directory.
     *
     * @param path the relative path to validate
     * @return the resolved Path object
     * @throws IllegalArgumentException if the path is outside the sandbox directory
     */
    private Path resolveAndValidatePath(String path) {
        Path filePath = basePath.resolve(path).normalize();

        if (!filePath.startsWith(basePath)) {
            throw new IllegalArgumentException(
                "Access denied: path must be within base directory (" + basePath + ")"
            );
        }

        return filePath;
    }

    /**
     * Returns the base path used by this adapter.
     *
     * @return the base path
     */
    public Path getBasePath() {
        return basePath;
    }
}