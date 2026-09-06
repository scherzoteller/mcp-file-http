package com.example.mcp.domain.file.ports;

/**
 * Port for file operations.
 * This interface defines the contract for file reading and writing operations.
 */
public interface FilePort {

    /**
     * Reads the content of a file.
     *
     * @param path the relative path to the file within the sandbox directory
     * @return the UTF-8 content of the file
     * @throws IllegalArgumentException if the path is outside the sandbox directory
     * @throws IllegalStateException if the file cannot be read
     */
    String readFile(String path);

    /**
     * Writes content to a file.
     *
     * @param path the relative path to the file within the sandbox directory
     * @param content the UTF-8 content to write to the file
     * @throws IllegalArgumentException if the path is outside the sandbox directory
     * @throws IllegalStateException if the file cannot be written
     */
    void writeFile(String path, String content);
}
