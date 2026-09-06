package com.example.mcp;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * Spring AI tools for local file operations.
 * These tools are registered with the MCP server and can be invoked
 * by local models to read and write files in the sandbox directory.
 */
@Component
@Slf4j
public class FileTools {

    private final FileOperationService fileService;

    public FileTools(FileOperationService fileService) {
        this.fileService = fileService;
    }

    /**
     * Reads the content of a text file from the sandbox directory.
     * 
     * @param path The relative path to the file within the base sandbox directory.
     * @return The UTF-8 content of the file.
     */
    @McpTool(
        name = "read_file",
        description = "Read the content of a text file from the sandbox directory. The path must be relative to the base sandbox directory."
    )
    public String readFile(@McpToolParam(description = "relative path of file") String path) {
        log.debug("FileTools.readFile called with path: {}", path);
        String result = fileService.readFile(path);
        log.info("read_file completed successfully for path: {}", path);
        return result;
    }

    /**
     * Writes content to a text file in the sandbox directory.
     * 
     * @param path The relative path to the file within the base sandbox directory.
     * @param content The UTF-8 content to write to the file.
     * @return Confirmation message.
     */
    @McpTool(
        name = "write_file", 
        description = "Write content to a text file in the sandbox directory. The path must be relative to the base sandbox directory."
    )
    public String writeFile(@McpToolParam(description = "relative path of file") String path, @McpToolParam(description = "file content") String content) {
        log.debug("FileTools.writeFile called with path: {}, contentLength: {}", path, content != null ? content.length() : 0);
        fileService.writeFile(path, content);
        log.info("write_file completed successfully for path: {}", path);
        return "File written successfully: " + path;
    }

    /**
     * Returns a list of available tools.
     */
    @McpTool(
        name = "list_tools",
        description = "Get a list of available tools and their descriptions."
    )
    public String listTools() {
        log.debug("FileTools.listTools called");
        return "Available tools:\n" +
                "1. read_file(path) - Read the content of a text file from the sandbox directory.\n" +
                "2. write_file(path, content) - Write content to a text file in the sandbox directory.";
    }
}