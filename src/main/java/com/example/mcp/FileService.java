package com.example.mcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.*;

@Service
public class FileService {
    private final Path baseDir;
    public FileService(@Value("${mcp.base-dir}") String baseDir) throws Exception {
        this.baseDir = Paths.get(baseDir).toRealPath();
    }
    private Path safePath(String userPath) {
        Path resolved = baseDir.resolve(userPath).normalize();
        // For writes the file may not exist yet, so we only check normalized path
        if (!resolved.startsWith(baseDir)) {
            throw new SecurityException("Access denied: path escapes base directory: " + userPath);
        }
        return resolved;
    }
    public String readFile(String userPath) throws Exception {
        Path p = safePath(userPath);
        Path real = p.toRealPath();
        if (!real.startsWith(baseDir)) throw new SecurityException("Symlink escape");
        if (!Files.isRegularFile(real)) throw new NoSuchFileException(userPath);
        return Files.readString(real);
    }
    public void writeFile(String userPath, String content) throws Exception {
        Path p = safePath(userPath);
        Files.createDirectories(p.getParent());
        Files.writeString(p, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
