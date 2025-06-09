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
3. prompt

Hãy dùng mcp tool thức hiện các bước sau :
1. Mở trình duyệt
2. Mở trang https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
3. Hãy login với user : Admin, Password : admin123
4. Sau khi login thành công sẽ đi đến trang Dashboard

*** Lưu ý ***
Hãy tự tìm locator là duy nhất trên trang web nếu ko được cung cấp
Hãy trả lại code sau khi chạy xong vào file code.feature. Dựa theo cú pháp ở tài liệu này https://karatelabs.github.io/karate/examples/ui-test/