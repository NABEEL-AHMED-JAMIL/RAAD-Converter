package com.raad.converter.util;

import com.raad.converter.api.RaadConversionApi;
import com.raad.converter.convergen.RaadStreamConverter;
import com.raad.converter.automate.EnvironmentManager;
import com.raad.converter.automate.Procedure;
import com.raad.converter.automate.RunEnvironment;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.*;


@Component
@Scope(value="prototype")
public class ScreenShoot implements Procedure {

    public Logger logger = LoggerFactory.getLogger(RaadConversionApi.class);

    public final String JPG = "JPG";
    public final String PNG = "PNG";
    public final String TEMP_SOURCE_FILE = "xyz.png";
    public final String TEMP_TARGET_FILE = "xyz.pdf";
    private WebDriver driver;
    private JavascriptExecutor javascriptExecutor;

    @Autowired
    private RaadStreamConverter raadStreamConverter;

    public ScreenShoot() { }

    //@PostConstruct
    public void startBrowser() {
        logger.info("Start Browser Done");
        EnvironmentManager.initWebDriver();
    }

    // max file limit is 100mb.
    public void startSnapShot(String url, String fileName) throws Exception {
        logger.info("File Path :- " + fileName);
        this.driver = RunEnvironment.getWebDriver();
        this.javascriptExecutor = (JavascriptExecutor) this.driver;
        this.driver.get(url);
        Thread.sleep(1000);
        if(this.isTargetPage(url)) {
            this.takeSnapShotFullScreen(fileName);
        }
    }

    public void startSnapShot(String url, ByteArrayOutputStream byteArrayOutputStream) throws Exception {
        this.driver = RunEnvironment.getWebDriver();
        this.javascriptExecutor = (JavascriptExecutor) this.driver;
        this.driver.get(url);
        Thread.sleep(1000);
        if(this.isTargetPage(url)) {
            this.takeSnapShotFullScreen(byteArrayOutputStream);
        }
    }

    public ByteArrayOutputStream startSnapShot(String url) throws Exception {
        this.driver = RunEnvironment.getWebDriver();
        this.javascriptExecutor = (JavascriptExecutor) this.driver;
        this.driver.get(url);
        Thread.sleep(1000);
        if(this.isTargetPage(url)) {
            return this.takeSnapShotFullScreen();
        }
        throw new Exception("Internal Server Error");
    }


    private Boolean isTargetPage(String pageUrl) {
        return pageUrl.equals(this.driver.getCurrentUrl()) == true ? true : false;
    }

    public void takeSnapShotHalfScreen(String fileWithPath) throws Exception {
        TakesScreenshot scrShot = ((TakesScreenshot)this.driver);
        FileUtils.copyFile(scrShot.getScreenshotAs(OutputType.FILE), new File(fileWithPath));
    }

    private void takeSnapShotFullScreen(String fileWithPath) throws Exception {
        ImageIO.write(new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(this.driver).getImage(), JPG, new File(fileWithPath));
    }

    private void takeSnapShotFullScreen(ByteArrayOutputStream byteArrayOutputStream) throws Exception {
        ImageIO.write(new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(this.driver).getImage(), JPG, byteArrayOutputStream);
        byteArrayOutputStream = this.raadStreamConverter.doConvert(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), TEMP_SOURCE_FILE, TEMP_TARGET_FILE);
        return;
    }

    private ByteArrayOutputStream takeSnapShotFullScreen() throws Exception {
        ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
        ImageIO.write(new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(this.driver).getImage(), PNG, imageArray);
        return this.raadStreamConverter.doConvert(new ByteArrayInputStream(imageArray.toByteArray()), TEMP_SOURCE_FILE, TEMP_TARGET_FILE);
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

    public int imageConvert(String url, String filePath) throws Exception {
        logger.info("Process Start");
        int returnValue = -1;
        Process p;
        if (System.getProperty("os.name").startsWith("Windows")) {
            p = Runtime.getRuntime().exec("cmd /C start chrome --headless --hide-scrollbars --disable-gpu --window-size=1280,1696 --screenshot=" + filePath + " --default-background-color=0 " + url);
        } else {
            p = Runtime.getRuntime().exec("google-chrome --headless --hide-scrollbars --disable-gpu --window-size=1280,1696 --screenshot=" + filePath + " --default-background-color=0 " + url);
        }
        p.waitFor();
        returnValue = p.exitValue();
        logger.info("Process Complete.");
        return returnValue;
    }

    //@PreDestroy
    public void stopBrowser() {
        logger.info("Stop Browser Done");
        EnvironmentManager.shutDownDriver();
    }

}
