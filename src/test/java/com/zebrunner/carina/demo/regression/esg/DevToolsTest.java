package com.zebrunner.carina.demo.regression.esg;

import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.zebrunner.carina.core.AbstractTest;
import com.zebrunner.carina.webdriver.DriverHelper;
import com.zebrunner.carina.utils.R;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.decorators.Decorated;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;

public class DevToolsTest extends AbstractTest {
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testHoldSessionWithCarinaDevtoolsUrl() throws Exception {
        long holdMillis = Long.parseLong(System.getProperty("cdp.hold.ms", "300000"));
        long pingIntervalMs = Long.parseLong(System.getProperty("cdp.ping.ms", "10000"));
        String initialUrl = System.getProperty("cdp.hold.url", "https://example.com");

        WebDriver webDriver = getDriver();
        RemoteWebDriver driver = unwrapRemoteDriver(webDriver);
        String sessionId = driver.getSessionId().toString();

        driver.get(initialUrl);

        String seleniumUrlStr = R.CONFIG.get("selenium_url");
        String carinaBase = seleniumUrlStr
                .replace("/wd/hub", "/devtools/")
                .replaceFirst("(^.+@)|(http(s)?://)", "wss://");
        String browserWsUrl = carinaBase + sessionId + "/";
        String pageWsUrl = carinaBase + sessionId + "/page";

        LOGGER.info("=====================================================================");
        LOGGER.info(" CDP session HELD OPEN — connect manually with websocat              ");
        LOGGER.info("---------------------------------------------------------------------");
        LOGGER.info(" sessionId     : {}", sessionId);
        LOGGER.info(" browser WS    : {}", browserWsUrl);
        LOGGER.info(" page WS       : {}  (append /<target-id> for a specific page)", pageWsUrl);
        LOGGER.info(" hold duration : {} ms ({} s)", holdMillis, holdMillis / 1000);
        LOGGER.info("");
        LOGGER.info(" websocat examples (NOTE: auth is stripped in Carina's URL — most");
        LOGGER.info(" likely the /devtools/* path does not require basic-auth at the ALB):");
        LOGGER.info("   websocat -v \"{}\"", browserWsUrl);
        LOGGER.info("   websocat -v \"{}\"", pageWsUrl);
        LOGGER.info("");
        LOGGER.info(" Once connected, send:");
        LOGGER.info("   {\"id\":1,\"method\":\"Browser.getVersion\"}");
        LOGGER.info("   {\"id\":2,\"method\":\"Target.getTargets\"}");
        LOGGER.info("=====================================================================");

        ChromeDevToolsService browserDevTools = null;
        try {
            DriverHelper helper = new DriverHelper(webDriver);
            browserDevTools = helper.browserChromeDevTools();
            String jsVersion = browserDevTools.getBrowser().getVersion().getJsVersion();
            LOGGER.info("Carina helper connected OK — browser.jsVersion={} (proves URL is reachable)",
                    jsVersion);
        } catch (Exception e) {
            LOGGER.warn("Carina helper failed to connect: {}. The session is still held open, "
                    + "you can still try websocat against the URLs above.", e.toString());
        }

        try {
            long deadline = System.currentTimeMillis() + holdMillis;
            long pingId = 0;
            while (System.currentTimeMillis() < deadline) {
                pingId++;
                try {
                    String currentUrl = driver.getCurrentUrl();
                    long remainingMs = Math.max(0, deadline - System.currentTimeMillis());
                    LOGGER.info("keepalive #{} — session alive (remaining={}s, url={})",
                            pingId, remainingMs / 1000, currentUrl);
                } catch (Exception e) {
                    LOGGER.error("Session died during keepalive (ping #{})", pingId, e);
                    throw e;
                }
                long sleepFor = Math.min(pingIntervalMs,
                        Math.max(0, deadline - System.currentTimeMillis()));
                if (sleepFor > 0) {
                    Thread.sleep(sleepFor);
                }
            }
        } finally {
            if (browserDevTools != null) {
                try {
                    browserDevTools.close();
                    browserDevTools.waitUntilClosed();
                } catch (Exception ignored) {
                }
            }
        }

        LOGGER.info("Hold period complete. Session will be closed by TestNG teardown.");
    }

    private RemoteWebDriver unwrapRemoteDriver(WebDriver webDriver) {
        if (webDriver instanceof Decorated<?>) {
            return (RemoteWebDriver) ((Decorated<?>) webDriver).getOriginal();
        }
        return (RemoteWebDriver) webDriver;
    }
}