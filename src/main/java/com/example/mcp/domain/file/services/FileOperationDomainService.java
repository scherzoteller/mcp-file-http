//package com.example.mcp.domain.file.services;
//
//import com.example.mcp.domain.file.ports.FilePort;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Objects;
//
///**
// * Domain service for file operations.
// * Implements business rules for file access within a sandbox directory.
// */
//public class FileOperationDomainService implements FilePort {
//
//    private final Path basePath;
//
//    /**
//     * Creates a FileOperationDomainService with the default sandbox directory.
//     */
//    public FileOperationDomainService() {
//        this.basePath = Paths.get("data/sandbox");
//    }
//
//    /**
//     * Creates a FileOperationDomainService with a custom base directory.
//     *
//     * @param basePath the base directory for file operations
//     */
//    public FileOperationDomainService(Path basePath) {
//        this.basePath = Objects.requireNonNull(basePath, "basePath cannot be null");
//    }
//
//    @Override
//    public String readFile(String path) {
//        Path filePath = resolveAndValidatePath(path);
//
//        try {
//            String content = Files.readString(filePath, StandardCharsets.UTF_8);
//            return content;
//        } catch (IOException e) {
//            throw new IllegalStateException("Failed to read file: " + path, e);
//        }
//    }
//
//    @Override
//    public void writeFile(String path, String content) {
//        Path filePath = resolveAndValidatePath(path);
//
//        try {
//            // Create parent directories if they don't exist
//            Files.createDirectories(filePath.getParent());
//
//            // Handle null content by writing empty string
//            String contentToWrite = content != null ? content : "";
//            Files.writeString(filePath, contentToWrite, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            throw new IllegalStateException("Failed to write file: " + path, e);
//        }
//    }
//
//    /**
//     * Resolves and validates the path to ensure it's within the base directory.
//     *
//     * @param path the relative path to validate
//     * @return the resolved Path object
//     * @throws IllegalArgumentException if the path is outside the sandbox directory
//     */
//    private Path resolveAndValidatePath(String path) {
//        Path filePath = basePath.resolve(path).normalize();
//
//        if (!filePath.startsWith(basePath)) {
//            throw new IllegalArgumentException(
//                "Access denied: path must be within base directory (" + basePath + ")"
//            );
//        }
//
//        return filePath;
//    }
//
//    /**
//     * Returns the base path used by this service.
//     *
//     * @return the base path
//     */
//    public Path getBasePath() {
//        return basePath;
//    }
//}