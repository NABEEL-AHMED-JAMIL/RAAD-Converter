package com.raad.converter.decoder;

/*
* Note :- This class use the selenium and open-cv for decode the text
* */
import com.raad.converter.automate.EnvironmentManager;
import com.raad.converter.automate.Procedure;
import com.raad.converter.automate.RunEnvironment;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.UUID;


@Component
@Scope("prototype")
public class TextCaptchaDecoder implements Procedure {

    public Logger logger = LoggerFactory.getLogger(TextCaptchaDecoder.class);

    public final String SRC = "src";
    public final String BLANK = "";
    private WebDriver driver;
    private JavascriptExecutor javascriptExecutor;
    private ITesseract instance;

    //@PostConstruct
    public void startBrowser() {
        logger.info("Start Browser Done");
        EnvironmentManager.initWebDriver();
        this.instance = new Tesseract();
        this.instance.setDatapath("C:\\Users\\Nabeel.Ahmed\\Downloads\\RAAD-Converter\\tessdata");
    }

    public void init() throws Exception {
        this.instance = new Tesseract();
        BufferedImage inputImage = ImageIO.read(new File("C:\\Users\\Nabeel.Ahmed\\Downloads\\captcha_image.pngresult.png"));
        BufferedImage outputImage = imageProcess(inputImage);
        String imgText = this.instance.doOCR(outputImage);
        Thread.sleep(1000);
        ImageIO.write(outputImage, "png", new File("C:\\Users\\Nabeel.Ahmed\\Downloads\\"+ UUID.randomUUID()+".png"));
        System.out.println("Text " + imgText);
    }

    public void webProcess(TargetTag targetTagDetail) throws Exception {
        logger.info("Target Url For Text Captcha :- " + targetTagDetail.getUrl());
        this.driver = RunEnvironment.getWebDriver();
        this.javascriptExecutor = (JavascriptExecutor) this.driver;
        this.driver.get(targetTagDetail.getUrl());
        Thread.sleep(1000);
        if(this.isTargetPage(targetTagDetail.getUrl())) {
            BufferedImage bufferedImage = this.getBufferImageFromTargetSite(targetTagDetail);
            if(bufferedImage != null) {
                if(targetTagDetail.getInputTag() != null) {
                    String imgText = this.instance.doOCR(bufferedImage);
                    System.out.println("Text " + imgText);
                    WebElement webElement = this.driver.findElement(By.cssSelector(targetTagDetail.getInputTag()));
                    webElement.sendKeys(imgText);
                    Thread.sleep(2000);
                }
                return;
            }
            throw new Exception("Image Not Found Exception");
        }
    }

    private BufferedImage getBufferImageFromTargetSite(TargetTag targetTagDetail) throws Exception {
        WebElement logo = this.driver.findElement(By.cssSelector(targetTagDetail.getImageTag()));
        String logoSRC = logo.getAttribute(SRC);
        if(logoSRC != null && !logoSRC.equals(BLANK) ) {
            URL imageURL = new URL(logoSRC);
            System.out.println(imageURL);
            return ImageIO.read(imageURL);
        }
        throw new Exception("Image Url Not Found");
    }

    public BufferedImage imageProcess(BufferedImage ipimage) {
        double d = ipimage.getRGB(ipimage.getWidth()/2, ipimage.getHeight()/2);
        float scaleFactor = 0f;
        float offset = 0f;
        if (d >= -1.4211511E7 && d < -7254228) {
            scaleFactor= 4f;
            offset = -10f;
        }
        BufferedImage opimage = new BufferedImage(ipimage.getWidth(), ipimage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphic = opimage.createGraphics();
        graphic.drawImage(ipimage, 0, 0, Color.BLACK, null);
        graphic.dispose();
        RescaleOp rescale = new RescaleOp(scaleFactor, offset, null);
        return rescale.filter(opimage, null);
//        Graphics2D graphic = opimage.createGraphics();
//        graphic.setBackground(Color.GRAY);
//        graphic.clearRect(0, 0, ipimage.getWidth(), ipimage.getHeight());
//        graphic.setColor(Color.BLACK);
//        graphic.drawImage(ipimage, 0, 0, ipimage.getWidth(), ipimage.getHeight(), null);
//        graphic.dispose();

    }

    private Boolean isTargetPage(String pageUrl) {
        return pageUrl.equals(this.driver.getCurrentUrl()) == true ? true : false;
    }

    //@PreDestroy
    public void stopBrowser() {
        logger.info("Stop Browser Done");
        EnvironmentManager.shutDownDriver();
    }

    public static void main(String args[]) throws Exception {
        TextCaptchaDecoder textCaptchaDecoder = new TextCaptchaDecoder();
        textCaptchaDecoder.init();
//        try {
//            textCaptchaDecoder.startBrowser();
//            textCaptchaDecoder.webProcess(getTagDetail());
//        } catch (Exception ex) {
//            System.out.println("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
//        } finally {
//            textCaptchaDecoder.stopBrowser();
//        }
    }

    public static TargetTag getTagDetail() {
        TargetTag targetTag = new TargetTag();
        targetTag.setUrl("http://ctri.nic.in/Clinicaltrials/advancesearchmain.php");
        targetTag.setImageTag("body > div > center > div:nth-child(3) > form > div > center > table:nth-child(1) > tbody > tr:nth-child(11) > td:nth-child(2) > img");
        targetTag.setInputTag("#T4");
        return targetTag;
    }
}
