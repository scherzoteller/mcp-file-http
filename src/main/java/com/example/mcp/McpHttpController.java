package com.example.mcp;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class McpHttpController {
    private final FileService fileService;
    private final ObjectMapper mapper = new ObjectMapper();

    public McpHttpController(FileService fileService) { this.fileService = fileService; }

    @PostMapping(value = "/mcp", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> handle(@RequestBody JsonNode req) throws Exception {
        int id = req.path("id").asInt(-1);
        String method = req.path("method").asText();
        ObjectNode resp = mapper.createObjectNode();
        resp.put("jsonrpc","2.0");
        resp.put("id", id);

        try {
            switch (method) {
                case "initialize" -> {
                    ObjectNode result = mapper.createObjectNode();
                    result.put("protocolVersion","2026-07-01");
                    ObjectNode server = mapper.createObjectNode();
                    server.put("name","mcp-file-server-http");
                    server.put("version","0.1");
                    result.set("serverInfo", server);
                    resp.set("result", result);
                }
                case "tools/list" -> {
                    ObjectNode result = mapper.createObjectNode();
                    var tools = mapper.createArrayNode();
                    var read = mapper.createObjectNode();
                    read.put("name","read_file");
                    read.put("description","Read UTF-8 file inside base directory");
                    read.set("inputSchema", mapper.createObjectNode().put("type","object"));
                    var write = mapper.createObjectNode();
                    write.put("name","write_file");
                    write.put("description","Write file inside base directory");
                    write.set("inputSchema", mapper.createObjectNode().put("type","object"));
                    tools.add(read); tools.add(write);
                    result.set("tools", tools);
                    resp.set("result", result);
                }
                case "tools/call" -> {
                    String name = req.path("params").path("name").asText();
                    JsonNode args = req.path("params").path("arguments");
                    if ("read_file".equals(name)) {
                        String path = args.path("path").asText();
                        String content = fileService.readFile(path);
                        ObjectNode result = mapper.createObjectNode();
                        var arr = mapper.createArrayNode();
                        var c = mapper.createObjectNode(); c.put("type","text"); c.put("text",content);
                        arr.add(c); result.set("content", arr);
                        resp.set("result", result);
                    } else if ("write_file".equals(name)) {
                        String path = args.path("path").asText();
                        String content = args.path("content").asText();
                        fileService.writeFile(path, content);
                        ObjectNode result = mapper.createObjectNode();
                        var arr = mapper.createArrayNode();
                        var c = mapper.createObjectNode(); c.put("type","text"); c.put("text","Wrote "+path);
                        arr.add(c); result.set("content", arr);
                        resp.set("result", result);
                    } else {
                        throw new IllegalArgumentException("Unknown tool");
                    }
                }
                default -> throw new IllegalArgumentException("Method not found");
            }
        } catch (Exception e) {
            resp.set("error", mapper.createObjectNode()
                .put("code", -32000)
                .put("message", e.getMessage()));
        }
        return ResponseEntity.ok(mapper.writeValueAsString(resp));
    }
}
