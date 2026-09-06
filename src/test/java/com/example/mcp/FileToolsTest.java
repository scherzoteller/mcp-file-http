package com.example.mcp;

import com.example.mcp.application.file.services.FileOperationAppService;
import com.example.mcp.domain.file.ports.FilePort;
import com.example.mcp.exposition.file.mcp.FileMcpTools;
import com.example.mcp.infrastructure.file.adapter.FileSystemFileAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for FileMcpTools using real FileSystemFileAdapter with temp directories.
 */
class FileToolsTest {

    private FileMcpTools fileMcpTools;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Create a FileSystemFileAdapter with our temp directory
        FilePort filePort = new FileSystemFileAdapter(tempDir);
        FileOperationAppService fileAppService = new FileOperationAppService(filePort);
        fileMcpTools = new FileMcpTools(fileAppService);
    }

    @Test
    void readFile_shouldReadContentFromValidFile() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "File content here");

        // When
        String result = fileMcpTools.readFile("test.txt");

        // Then
        assertThat(result).isEqualTo("File content here");
    }

    @ParameterizedTest
    @ValueSource(strings = {"file.txt", "subdir/file.txt", "a/b/c/deep.txt"})
    void readFile_shouldHandleNestedPaths(String path) throws IOException {
        // Given
        Path targetPath = tempDir.resolve(path);
        Files.createDirectories(targetPath.getParent());
        Files.writeString(targetPath, "Content for " + path);

        // When
        String result = fileMcpTools.readFile(path);

        // Then
        assertThat(result).isEqualTo("Content for " + path);
    }

    @Test
    void readFile_shouldHandleServiceException() {
        // When & Then - reading non-existent file should throw
        assertThatThrownBy(() -> fileMcpTools.readFile("nonexistent.txt"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to read file");
    }

    @Test
    void writeFile_shouldDelegateToServiceAndReturnConfirmation() throws IOException {
        // When
        String result = fileMcpTools.writeFile("test.txt", "Content to write");

        // Then
        assertThat(result).isEqualTo("File written successfully: test.txt");
        assertThat(Files.exists(tempDir.resolve("test.txt"))).isTrue();
        assertThat(Files.readString(tempDir.resolve("test.txt"))).isEqualTo("Content to write");
    }

    @ParameterizedTest
    @ValueSource(strings = {"newfile.txt", "existing.txt", "dir/newfile.txt"})
    void writeFile_shouldCorrectlyDelegateVariousPaths(String path) throws IOException {
        // When
        String result = fileMcpTools.writeFile(path, "Content");

        // Then
        assertThat(result).startsWith("File written successfully:");
        Path filePath = tempDir.resolve(path);
        assertThat(Files.exists(filePath)).isTrue();
        assertThat(Files.readString(filePath)).isEqualTo("Content");
    }

    @Test
    void writeFile_shouldHandleNullContent() throws IOException {
        // When
        String result = fileMcpTools.writeFile("null.txt", null);

        // Then
        assertThat(result).isEqualTo("File written successfully: null.txt");
        // null content creates an empty file
        assertThat(Files.exists(tempDir.resolve("null.txt"))).isTrue();
        assertThat(Files.readString(tempDir.resolve("null.txt"))).isEmpty();
    }

    @Test
    void writeFile_shouldHandleServiceException() {
        // When & Then - path traversal should be rejected
        assertThatThrownBy(() -> fileMcpTools.writeFile("../../etc/passwd", "content"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Access denied");
    }

    @Test
    void listTools_shouldReturnToolDescriptions() {
        // When
        String result = fileMcpTools.listTools();

        // Then
        assertThat(result)
            .contains("read_file")
            .contains("write_file")
            .contains("Available tools:")
            .contains("1. read_file(path)")
            .contains("2. write_file(path, content)");
    }

    @Test
    void listTools_shouldNotThrowException() {
        // When
        String result = fileMcpTools.listTools();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    /**
     * Test data provider for round-trip file operations
     */
    static Stream<Arguments> roundTripProvider() {
        return Stream.of(
            Arguments.of("test1.txt", "Simple content"),
            Arguments.of("test2.txt", "Content with special chars: !@#$%^&*()"),
            Arguments.of("test3.txt", "Multi\nline\ncontent"),
            Arguments.of("test4.txt", "")
        );
    }

    @ParameterizedTest
    @MethodSource("roundTripProvider")
    void writeFile_and_readFile_shouldWorkTogether(String filename, String content) throws IOException {
        // When
        fileMcpTools.writeFile(filename, content);
        String result = fileMcpTools.readFile(filename);

        // Then
        assertThat(result).isEqualTo(content);
    }

    @Test
    void fileMcpTools_shouldHandleLongContent() throws IOException {
        // Given
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("Line ").append(i).append("\n");
        }

        // When
        fileMcpTools.writeFile("long.txt", longContent.toString());
        String result = fileMcpTools.readFile("long.txt");

        // Then
        assertThat(result).isEqualTo(longContent.toString());
    }

    @Test
    void fileMcpTools_shouldHandleEmptyFilenameComponents() throws IOException {
        // Given
        Path testFile = tempDir.resolve("empty.txt");
        Files.writeString(testFile, "");

        // When
        String result = fileMcpTools.readFile("empty.txt");

        // Then
        assertThat(result).isEmpty();
    }
}