package com.example.karatemcp;

import com.intuit.karate.driver.chrome.Chrome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class BrowserTools {

    private static final Logger log = LoggerFactory.getLogger(BrowserTools.class);
    private Chrome driver;

    public Map<String, String> openChrome() {

        log.info("Attempting to open Chrome browser...");
        // Set the path to your ChromeDriver executable
        // You will need to download the ChromeDriver that matches your Chrome browser version
        try {

            driver = Chrome.start();
            driver.maximize();
            log.info("Chrome browser opened successfully.");
        } catch (Exception e) {
            log.error("Failed to open Chrome browser", e);
            // Handle exceptions, e.g., ChromeDriver not found, browser not installed, etc.
            return Map.of("error", "Failed to open Chrome browser" + e);
        }
        return Map.of("code",
                    """
                        * driver 'https://globedr.com'
                        """);
    }

    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
            log.info("Browser closed.");
            driver = null;
        }
    }

    public Map<String, String> input(String locator, String content) {
        Map<String, String> result = new HashMap<>();
        if (driver != null) {
            log.info(String.format("Browser input value '%s' to locator '%s' ", locator, content));
            driver.locate(locator).input(content);
            result.put("code", String.format("* waitFor('%s').input('%s')", locator, content));

            // Capture screenshot as base64
            result.put("screenshot", screenshot());

            // Get page source
            result.put("snapshot", pageSnapshot());
        }
        return result;
    }

    public Map<String, String> click(String locator) {
        Map<String, String> result = new HashMap<>();
        if (driver != null) {
            log.info(String.format("Browser click on locator '%s' ", locator, locator));
            driver.locate(locator).click();
            result.put("code", String.format("* waitFor('%s').click()", locator));
            // Capture screenshot as base64
            result.put("screenshot", screenshot());

            // Get page source
            result.put("snapshot", pageSnapshot());
        }
        return result;
    }

    public Map<String, String> navigate(String url) {
        Map<String, String> result = new HashMap<>();
        if (driver != null) {
            log.info(String.format("Browser navigate value '%s'", url));
            try {
                driver.setUrl(url);
                Thread.sleep(2000);
                // Capture screenshot as base64
                result.put("screenshot", screenshot());

                // Get page source
                result.put("snapshot", pageSnapshot());
                result.put("code", String.format("* driver '%s'", url));

            } catch (Exception e) {
                log.error("Failed to navigate or capture screenshot/source", e);
                result.put("error", e.getMessage());
            }
        }
        return result;
    }

    public String screenshot() {
        if (driver != null) {
            log.info(String.format("Browser screenshot"));
            try {
                Thread.sleep(2000);

                // Capture screenshot as base64
                return Base64.getEncoder().encodeToString(driver.screenshot());

            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public String pageSnapshot() {
        if (driver != null) {
            log.info(String.format("page snapshot"));
            try {
                // Capture screenshot as base64
                driver.retry(30,1000).waitFor("//body");
                return driver.locate("//body").script("_.innerHTML").toString();

            } catch (Exception e) {

            }
        }
        return null;
    }
} 