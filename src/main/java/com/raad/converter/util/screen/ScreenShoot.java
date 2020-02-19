package com.raad.converter.util.screen;

import com.raad.converter.api.ConversionController;
import com.raad.converter.convergen.RaadStreamConverter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;


@Component
@Scope(value="prototype")
public class ScreenShoot implements Procedure {

    public Logger logger = LogManager.getLogger(ConversionController.class);

    public final String JPG = "JPG";
    public final String PNG = "PNG";
    public final String TEMP_SOURCE_FILE = "xyz.png";
    public final String TEMP_TARGET_FILE = "xyz.pdf";
    private WebDriver driver;
    private JavascriptExecutor javascriptExecutor;

    @Autowired
    private RaadStreamConverter raadStreamConverter;

    public ScreenShoot() { }

    @PostConstruct
    public void startBrowser() {
        System.out.println("Start Browser Done");
        EnvironmentManager.initWebDriver();
    }

    // max file limit is 100mb.
    public void startSnapShot(String url, String fileName) throws Exception {
        System.out.println("File Path :- " + fileName);
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
    }

    private ByteArrayOutputStream takeSnapShotFullScreen() throws Exception {
        ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
        ImageIO.write(new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(this.driver).getImage(), PNG, imageArray);
        return this.raadStreamConverter.doConvert(new ByteArrayInputStream(imageArray.toByteArray()), TEMP_SOURCE_FILE, TEMP_TARGET_FILE);
    }

    @PreDestroy
    public void stopBrowser() {
        System.out.println("Stop Browser Done");
        EnvironmentManager.shutDownDriver();
    }

    public static void main(String args[]) throws Exception {
        ScreenShoot screenShoot = new ScreenShoot();
        screenShoot.startBrowser();
        screenShoot.startSnapShot("http://www.globenewswire.com/news-release/2020/02/18/1986454/0/en/Talenom-Plc-Disclosure-under-chapter-9-section-10-of-the-securities-market-act.html",
             "C:\\Users\\Nabeel.Ahmed\\Downloads\\RAAD-Converter\\src\\main\\resources\\image\\"+ UUID.randomUUID() +".png");
        screenShoot.stopBrowser();
    }
}
