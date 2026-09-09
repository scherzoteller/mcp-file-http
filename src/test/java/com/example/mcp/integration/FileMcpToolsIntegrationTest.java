package com.example.mcp.integration;

import com.example.mcp.application.file.services.FileOperationAppService;
import com.example.mcp.exposition.file.mcp.FileMcpTools;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileMcpToolsIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestMcpClientFactory testMcpClientFactory;

    @MockitoBean
    private FileOperationAppService fileOperationAppService;

    private McpSyncClient client;

    @BeforeEach
    void setUp() {
        client = testMcpClientFactory.create("http://localhost:" + port);
        client.initialize();
    }

    @Test
    void allToolsAreThere(){
        List<McpSchema.Tool> tools = client.listTools().tools();
        assertThat(tools).hasSize(3)
                .anyMatch(aTool -> "read_file".equals(aTool.name()), "tool read_file")
                .anyMatch(aTool -> "write_file".equals(aTool.name()), "tool write_file")
                .anyMatch(aTool -> "list_tools".equals(aTool.name()), "tool list_tools")
        ;
    }

    @Test
    void listTools(){
        McpSchema.CallToolResult result = client.callTool(
                new McpSchema.CallToolRequest("list_tools", Map.of(), Map.of())
        );
        List<McpSchema.Content> content = result.content();
        assertThat(content)
                .hasSize(1)
                        .allSatisfy(
                                c -> {
                                    assertThat(c.type()).isEqualTo("text");
                                    assertThat(((McpSchema.TextContent)c).text()).isEqualTo(FileMcpTools.LIST_TOOLS_RESULT);
                                }
                        );
    }

    @Test
    void writeFile() {
        String testContent = "Hello, MCP File HTTP Integration Test!";
        String filePath = "test/integration-test-file.txt";

        McpSchema.CallToolResult writeResult = client.callTool(
                new McpSchema.CallToolRequest("write_file", Map.of(
                        "path", filePath,
                        "content", testContent
                ), Map.of())
        );

        assertThat(writeResult.content())
                .hasSize(1)
                .allSatisfy(c -> {
                    assertThat(c.type()).isEqualTo("text");
                    assertThat(((McpSchema.TextContent) c).text()).contains("File written successfully");
                });
        verify(fileOperationAppService).writeFile(filePath, testContent);
    }
    @Test
    void readFile() {
        String testContent = "Hello, MCP File HTTP Integration Test!";
        String filePath = "test/integration-test-file.txt";

        when(fileOperationAppService.readFile(filePath)).thenReturn(testContent);
        McpSchema.CallToolResult readResult = client.callTool(
                new McpSchema.CallToolRequest("read_file", Map.of(
                        "path", filePath
                ), Map.of())
        );

        List<McpSchema.Content> readContent = readResult.content();
        assertThat(readContent)
                .hasSize(1)
                .allSatisfy(c -> {
                    assertThat(c.type()).isEqualTo("text");
                    assertThat(((McpSchema.TextContent) c).text()).isEqualTo(testContent);
                });
    }

    @AfterEach
    void cleanUp() {
        client.closeGracefully();
    }
}