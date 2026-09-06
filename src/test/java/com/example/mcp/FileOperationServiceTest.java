package com.example.mcp;

import org.junit.jupiter.api.Test;
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
 * Unit tests for FileOperationService.
 */
class FileOperationServiceTest {

    @Test
    void readFile_shouldReadContentFromExistingFile() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        Path testFile = sandboxDir.resolve("test.txt");
        Files.writeString(testFile, "Hello, World!");

        // Create a custom service with our test directory
        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        String content = service.readFile("test.txt");

        // Then
        assertThat(content).isEqualTo("Hello, World!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"file.txt", "subdir/file.txt", "a/b/c/deep.txt"})
    void readFile_shouldReadFilesInSubdirectories(String path) throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        Path targetPath = sandboxDir.resolve(path);
        Files.createDirectories(targetPath.getParent());
        Files.writeString(targetPath, "Content of " + path);

        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        String content = service.readFile(path);

        // Then
        assertThat(content).isEqualTo("Content of " + path);
    }

    @Test
    void readFile_shouldReturnEmptyStringForEmptyFile() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        Path testFile = sandboxDir.resolve("empty.txt");
        Files.writeString(testFile, "");

        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        String content = service.readFile("empty.txt");

        // Then
        assertThat(content).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"../../../etc/passwd", "../secret.txt", "/etc/shadow", "../../../etc/hosts"})
    void readFile_shouldRejectPathTraversalAttempts(String maliciousPath) throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When & Then
        assertThatThrownBy(() -> service.readFile(maliciousPath))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Access denied");
    }

    @Test
    void readFile_shouldThrowExceptionForNonExistentFile() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When & Then
        assertThatThrownBy(() -> service.readFile("nonexistent.txt"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to read file");
    }

    @Test
    void writeFile_shouldCreateNewFile() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        service.writeFile("newfile.txt", "New content");

        // Then
        Path filePath = sandboxDir.resolve("newfile.txt");
        assertThat(Files.exists(filePath)).isTrue();
        assertThat(Files.readString(filePath)).isEqualTo("New content");
    }

    @Test
    void writeFile_shouldOverwriteExistingFile() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        Path testFile = sandboxDir.resolve("existing.txt");
        Files.writeString(testFile, "Old content");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        service.writeFile("existing.txt", "New content");

        // Then
        assertThat(Files.readString(testFile)).isEqualTo("New content");
    }

    @Test
    void writeFile_shouldCreateSubdirectories() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        service.writeFile("a/b/c/deep.txt", "Deep content");

        // Then
        Path filePath = sandboxDir.resolve("a/b/c/deep.txt");
        assertThat(Files.exists(filePath)).isTrue();
        assertThat(Files.readString(filePath)).isEqualTo("Deep content");
    }

    @ParameterizedTest
    @ValueSource(strings = {"../../../etc/passwd", "../secret.txt", "/etc/shadow"})
    void writeFile_shouldRejectPathTraversalAttempts(String maliciousPath) throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When & Then
        assertThatThrownBy(() -> service.writeFile(maliciousPath, "content"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Access denied");
    }

    @Test
    void writeFile_shouldHandleNullContent() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        service.writeFile("nullcontent.txt", null);

        // Then - null content should write an empty file
        Path filePath = sandboxDir.resolve("nullcontent.txt");
        assertThat(Files.exists(filePath)).isTrue();
    }

    @Test
    void writeFile_shouldHandleUnicodeContent() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);
        String unicodeContent = "你好，世界！🌍 Привет мир!";

        // When
        service.writeFile("unicode.txt", unicodeContent);

        // Then
        Path filePath = sandboxDir.resolve("unicode.txt");
        assertThat(Files.readString(filePath)).isEqualTo(unicodeContent);
    }

    @Test
    void getBasePath_shouldReturnBaseDirectory() throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        Path basePath = service.getBasePath();

        // Then
        assertThat(basePath).isNotNull();
        assertThat(basePath).isEqualTo(sandboxDir);
    }

    /**
     * Test data provider for file content validation
     */
    static Stream<Arguments> fileContentProvider() {
        return Stream.of(
            Arguments.of("simple", "Hello"),
            Arguments.of("with spaces", "Hello World"),
            Arguments.of("with newlines", "Line 1\nLine 2\nLine 3"),
            Arguments.of("empty", "")
        );
    }

    @ParameterizedTest
    @MethodSource("fileContentProvider")
    void writeFile_and_readFile_shouldBeConsistent(String testName, String content) throws IOException {
        // Given
        Path sandboxDir = Files.createTempDirectory("sandbox-test");
        FileOperationService service = new FileOperationService(sandboxDir);

        // When
        service.writeFile(testName + ".txt", content);
        String readContent = service.readFile(testName + ".txt");

        // Then
        assertThat(readContent).isEqualTo(content);
    }
}