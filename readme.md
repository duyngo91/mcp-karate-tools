1. Run terminal : mvn clean package
2. Open Claude AI Application > File > Settings > Developer > Edit Config >  Add below config to claude_desktop_config.json
{
    "mcpServers": {
        "mcp-karate-tool": {
            "command": "java",
            "args": ["-jar", "E:/Project/auto/mcp-karate-tools/target/mcp-karate-1.0-SNAPSHOT.jar"],
            "env": { "JAVA_HOME": "C:\\Program Files\\Java\\jdk-22"}
        }
    }
}