package parser;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeleniumDriver {
    private RemoteWebDriver driver;
    private final WebDriverWait shortWait10;
    private final String seleniumHost = System.getenv("SELENIUM_HOST");
    private final String seleniumPort = System.getenv("SELENIUM_PORT");
    private final String seleniumPath = System.getenv("SELENIUM_PATH");

    public SeleniumDriver() throws MalformedURLException, URISyntaxException {
        runChromeDriver();
        this.shortWait10 = new WebDriverWait(this.driver, Duration.ofSeconds(20));
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

    public WebDriverWait getShortWait10() {
        return shortWait10;
    }

    public void runChromeDriver() throws MalformedURLException, URISyntaxException {
//        System.setProperty("web.driver.chrome", "usr/local/bin");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.INFO);
        ChromeOptions chromeOptions = getChromeOptions();
        URL urlHost = new URI("http://" + seleniumHost + ":" + seleniumPort + seleniumPath).toURL();
        driver = new RemoteWebDriver(
                urlHost, chromeOptions, false
        );
        String sessionId = driver.getSessionId().toString();
        System.out.println(sessionId);
//        driver = new ChromeDriver(getChromeOptions());
        System.out.println("REMOTE DRIVER HAS BEEN SUCCESSFULLY STARTED!");
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--log-level=ON");
        List<String> arguments = new LinkedList<>();
        arguments.add("--disable-extensions");
        arguments.add("--headless");
        arguments.add("--disable-gpu");
        arguments.add("--no-sandbox");
        arguments.add("--incognito");
        arguments.add("--disable-application-cache");
        arguments.add("--disable-dev-shm-usage");
        chromeOptions.addArguments(arguments);
        return chromeOptions;
    }

    public void sleepForSomeTime(int i) {
        try {
            Thread.sleep(i * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeDriver() {
        driver.close();
        driver.quit();
    }


}
