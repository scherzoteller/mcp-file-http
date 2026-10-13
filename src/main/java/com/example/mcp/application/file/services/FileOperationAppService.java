package com.example.mcp.application.file.services;

import com.example.mcp.domain.file.ports.FilePort;

import java.util.List;

/**
 * Application service for file operations.
 * Combines different parts of the domain layer to provide application-level functionality.
 */
public class FileOperationAppService {

    private final FilePort filePort;

    /**
     * Creates a FileOperationAppService with the given FilePort.
     *
     * @param filePort the port for file operations
     */
    public FileOperationAppService(FilePort filePort) {
        this.filePort = filePort;
    }

    /**
     * Reads a file from the sandbox directory.
     *
     * @param path the relative path to the file
     * @return the UTF-8 content of the file
     */
    public String readFile(String path) {
        return filePort.readFile(path);
    }

    /**
     * Writes content to a file in the sandbox directory.
     *
     * @param path the relative path to the file
     * @param content the content to write
     */
    public void writeFile(String path, String content) {
        filePort.writeFile(path, content);
    }

    public List<String> listFilesAndDirs(String path, boolean recurse){
        return filePort.listFilesAndDirs(path, recurse);
    }
}