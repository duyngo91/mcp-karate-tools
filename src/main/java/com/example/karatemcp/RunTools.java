package com.example.karatemcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RunTools {
    private static Logger log = LoggerFactory.getLogger(RunTools.class);
    private static BrowserTools browserTools = new BrowserTools();

    public static void main(String[] args) {
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider();
        List<McpServerFeatures.SyncToolSpecification>syncToolSpecification = getSyncToolSpecification();
        McpSyncServer syncServer = McpServer.sync(transportProvider)
                .serverInfo("javaone-mcp-server", "0.0.1")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .logging()
                        .build())
                // Register tools, resources, and prompts
                .tools(syncToolSpecification)
                .build();

        log.info("Starting JavaOne MCP Server...");
    }

    private static List<McpServerFeatures.SyncToolSpecification> getSyncToolSpecification() {


        String openBrowserSchema = """
                {
                  "type" : "object"
                }
                """;
        String inputToolSchema = """
                {
                  "type": "object",
                  "id" : "urn:jsonschema:Operation",
                  "properties": {
                    "locator": {
                      "type": "string",
                      "format": "xpath",
                      "description": "xpath locator of element"
                    },
                    "content": {
                      "type": "string",
                      "description": "input content into the element"
                    }
                  }
                }
                """;
        String navigateToolSchema = """
                {
                  "type": "object",
                  "id" : "urn:jsonschema:Operation",
                  "properties": {
                    "url": {
                      "type": "string",
                      "format": "url",
                      "description": "The URL to navigate to"
                    }
                  }
                }
                """;

        // Define a schema for the result of the navigate tool to include screenshot and page source
        String navigateResultSchema = """
                {
                  "type": "object",
                  "properties": {
                   "url": {
                      "type": "string",
                      "format": "url",
                      "description": "The URL to navigate to"
                    },
                    "screenshot": {
                      "type": "string",
                      "description": "Path or base64 of the screenshot"
                    },
                    "pageSnapshot": {
                      "type": "string",
                      "description": "The HTML source of the page"
                    },
                    "error": {
                       "type": "string",
                       "description": "Error message if navigation failed"
                    }
                  }
                }
                """;
        String screenshotSchema = """
                {
                  "type": "object",
                  "properties": {
                    "screenshot": {
                      "type": "string",
                      "description": "Path or base64 of the screenshot"
                    }
                  }
                }
                """;
        String snapshotSchema = """
                {
                  "type": "object",
                  "properties": {
                    "snapshot": {
                      "type": "string",
                      "description": "The HTML source of the page"
                    }
                  }
                }
                """;
        String clickToolSchema = """
                {
                  "type": "object",
                  "id" : "urn:jsonschema:Operation",
                  "properties": {
                    "locator": {
                      "type": "string",
                      "format": "xpath",
                      "description": "xpath locator of element"
                    }
                  }
                }
                """;

        McpSchema.Tool openBrowserTool = new McpSchema.Tool("open_browser", "Open Chrome browser using Selenium", openBrowserSchema);
        McpSchema.Tool inputTool = new McpSchema.Tool("browser_input", "input text into the element", inputToolSchema);
        McpSchema.Tool navigateTool = new McpSchema.Tool("browser_navigate", "browser navigate to a URL", navigateResultSchema);
        McpSchema.Tool screenshotTool = new McpSchema.Tool("browser_screenshot", "Take a screenshot of the current page", screenshotSchema);
        McpSchema.Tool snapshotTool = new McpSchema.Tool("browser_snapshot", "Capture accessibility snapshot of the current page, this is better than screenshot", snapshotSchema);
        McpSchema.Tool clickTool = new McpSchema.Tool("browser_click", "browser click on the element", clickToolSchema);


        McpServerFeatures.SyncToolSpecification openBrowserHandler = new McpServerFeatures.SyncToolSpecification(openBrowserTool,
                (exchange, args) -> {
                    List<McpSchema.Content> contents = new ArrayList<>();
                    Map<String, String> rs =  browserTools.openChrome();
                    contents.add(new McpSchema.TextContent("code: " + rs.get("code")));
                    contents.add(new McpSchema.TextContent("error: " + rs.get("error")));
                    contents.add(new McpSchema.TextContent("Chrome browser is being opened."));
                    return new McpSchema.CallToolResult(contents, false);
                });


        McpServerFeatures.SyncToolSpecification inputToolHandler = new McpServerFeatures.SyncToolSpecification(inputTool,
                (exchange, args) -> {
                    List<McpSchema.Content> contents = new ArrayList<>();
                    Map<String, String> rs = browserTools.input(args.get("locator").toString(), args.get("content").toString());
                    contents.add(new McpSchema.TextContent("code: " + rs.get("code")));
                    contents.add(new McpSchema.TextContent("input text into the element"));
                    return new McpSchema.CallToolResult(contents, false);
                });
        McpServerFeatures.SyncToolSpecification clickToolHandler = new McpServerFeatures.SyncToolSpecification(clickTool,
                (exchange, args) -> {
                    List<McpSchema.Content> contents = new ArrayList<>();
                    Map<String, String> rs = browserTools.click(args.get("locator").toString());
                    contents.add(new McpSchema.TextContent("code: " + rs.get("code")));
                    contents.add(new McpSchema.TextContent("click on the element"));
                    return new McpSchema.CallToolResult(contents, false);
                });

        McpServerFeatures.SyncToolSpecification navigateToolHandler = new McpServerFeatures.SyncToolSpecification(navigateTool,
                (exchange, args) -> {
                    Map<String, String> navigationResult = browserTools.navigate(args.get("url").toString());
                    List<McpSchema.Content> contents = new ArrayList<>();

                    // Add screenshot and page source to the result
                    if (navigationResult.containsKey("screenshot")) {
                         contents.add(new McpSchema.ImageContent(Arrays.asList(McpSchema.Role.USER),1.0,navigationResult.get("screenshot"), "image/png"));
                    }
                    if (navigationResult.containsKey("snapshot")) {
                         contents.add(new McpSchema.TextContent("Snapshot (Source): " + navigationResult.get("snapshot"))); // Limit output size
                    }
                     if (navigationResult.containsKey("error")) {
                         contents.add(new McpSchema.TextContent("Error: " + navigationResult.get("error")));
                    }

                    return new McpSchema.CallToolResult(contents, false);
                });

        McpServerFeatures.SyncToolSpecification screenshotToolHandler = new McpServerFeatures.SyncToolSpecification(screenshotTool,
                (exchange, args) -> {
                    String result = browserTools.screenshot();
                    List<McpSchema.Content> contents = new ArrayList<>();
                    contents.add(new McpSchema.ImageContent(Arrays.asList(McpSchema.Role.USER),1.0, result, "image/png"));
                    return new McpSchema.CallToolResult(contents, false);
                });

        McpServerFeatures.SyncToolSpecification snapshotToolHandler = new McpServerFeatures.SyncToolSpecification(snapshotTool,
                (exchange, args) -> {
                    String result = browserTools.pageSnapshot();
                    List<McpSchema.Content> contents = new ArrayList<>();
                    contents.add(new McpSchema.TextContent("snapshot: " + result));
                    return new McpSchema.CallToolResult(contents, false);
                });

        List<McpServerFeatures.SyncToolSpecification> tools = new ArrayList<>();
        tools.add(openBrowserHandler);
        tools.add(inputToolHandler);
        tools.add(navigateToolHandler);
        tools.add(screenshotToolHandler);
        tools.add(snapshotToolHandler);
        tools.add(clickToolHandler);

        return tools;
    }

}
