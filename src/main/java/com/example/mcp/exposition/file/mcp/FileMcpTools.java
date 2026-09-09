package com.example.mcp.exposition.file.mcp;

import com.example.mcp.application.file.services.FileOperationAppService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tools for file operations.
 * This class is part of the exposition layer and provides MCP tools
 * that can be invoked by local models to read and write files.
 */
@Component
public class FileMcpTools {

    public static final String LIST_TOOLS_RESULT = """
            Available tools:
            1. read_file(path) - Read the content of a text file from the sandbox directory.
            2. write_file(path, content) - Write content to a text file in the sandbox directory.
            """;
    private final FileOperationAppService fileAppService;

    /**
     * Creates a FileMcpTools with the given application service.
     *
     * @param fileAppService the application service for file operations
     */
    public FileMcpTools(FileOperationAppService fileAppService) {
        this.fileAppService = fileAppService;
    }

    /**
     * Reads the content of a text file from the sandbox directory.
     *
     * @param path the relative path to the file within the base sandbox directory
     * @return the UTF-8 content of the file
     */
    @McpTool(
        name = "read_file",
        description = "Read the content of a text file from the sandbox directory. The path must be relative to the base sandbox directory."
    )
    public String readFile(@McpToolParam(description = "relative path of file") String path) {
        return fileAppService.readFile(path);
    }

    /**
     * Writes content to a text file in the sandbox directory.
     *
     * @param path the relative path to the file within the base sandbox directory
     * @param content the UTF-8 content to write to the file
     * @return confirmation message
     */
    @McpTool(
        name = "write_file",
        description = "Write content to a text file in the sandbox directory. The path must be relative to the base sandbox directory."
    )
    public String writeFile(@McpToolParam(description = "relative path of file") String path, @McpToolParam(description = "file content") String content) {
        fileAppService.writeFile(path, content);
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
        return LIST_TOOLS_RESULT;
    }
}