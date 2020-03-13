package com.raad.converter.util;


import com.raad.converter.automate.RunEnvironment;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
@Scope(value="prototype")
public class HeaderLessBrowser {

    public Logger logger = LoggerFactory.getLogger(HeaderLessBrowser.class);

    public final String JPG = "JPG";
    public final String PNG = "PNG";
    private WebDriver driver;
    private JavascriptExecutor javascriptExecutor;

    private static final String SANDBOK = "--no-sandbox";
    private static final String CERTIFICATE_ERROR = "--ignore-certificate-errors";
    private static final String START_MAXIMIZED = "--start-maximized";
    private static final String DISABLE_DEV_SHM_USAGE = "--disable-dev-shm-usage";
    private static final String INCONGNITO = "--incognito";
    private static final String HEADLESS = "--headless";
    private static final String START_FULL_SCREEN = "start-fullscreen";


    public static void initWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(SANDBOK);
        options.addArguments(CERTIFICATE_ERROR);
        options.addArguments(START_MAXIMIZED);
        options.addArguments(DISABLE_DEV_SHM_USAGE);
        options.addArguments(INCONGNITO);
        options.addArguments(HEADLESS);
        options.addArguments(START_FULL_SCREEN);
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        RunEnvironment.setWebDriver(new ChromeDriver(capabilities));
    }

    // max file limit is 100mb.
    public int startSnapShot(String url, String filePath) throws IOException {
        int returnValue = -1;
        FileOutputStream fos = null;
        try {
            logger.info("File Path :- " + filePath);
            this.driver = RunEnvironment.getWebDriver();
            this.javascriptExecutor = (JavascriptExecutor) this.driver;
            this.driver.get(url);
            Thread.sleep(1000);
            if(this.isTargetPage(url)) {
                ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
                ImageIO.write(new AShot().
                    shootingStrategy(ShootingStrategies.viewportPasting(1000)).
                    takeScreenshot(this.driver).getImage(), PNG, imageArray);
                fos = new FileOutputStream(new File(filePath));
                imageArray.writeTo(fos);
                returnValue = 1;
                return returnValue;
            }
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
            return returnValue;
        } finally {
            fos.close();
        }
        return returnValue;
    }

    public int htmlConvert(String url, String filePath) throws Exception {
        logger.info("Process Start");
        int returnValue = -1;
        Process p;
        if (System.getProperty("os.name").startsWith("Windows")) {
            p = Runtime.getRuntime().exec("cmd /C start chrome --headless --hide-scrollbars --disable-gpu --no-sandbox --print-to-pdf=" + filePath + " " + url);
        } else {
            p = Runtime.getRuntime().exec("google-chrome --headless --hide-scrollbars --disable-gpu --no-sandbox --print-to-pdf=" + filePath + " " + url);
        }
        p.waitFor();
        returnValue = p.exitValue();
        logger.info("Process Complete.");
        return returnValue;
    }

    private Boolean isTargetPage(String pageUrl) {
        return pageUrl.equals(this.driver.getCurrentUrl()) == true ? true : false;
    }

    // stop the driver
    public static void shutDownDriver() {
        RunEnvironment.getWebDriver().close();
    }
}
