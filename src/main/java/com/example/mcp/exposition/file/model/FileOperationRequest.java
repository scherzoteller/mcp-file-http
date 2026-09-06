package com.example.mcp.exposition.file.model;

/**
 * DTO for file operation requests.
 */
public class FileOperationRequest {

    private String path;
    private String content;

    public FileOperationRequest() {
    }

    public FileOperationRequest(String path, String content) {
        this.path = path;
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}