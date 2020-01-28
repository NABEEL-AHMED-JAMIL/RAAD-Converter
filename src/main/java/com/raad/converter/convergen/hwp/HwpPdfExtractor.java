package com.raad.converter.convergen.hwp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;


@Slf4j
@Service
@Scope(value="prototype")
public class HwpPdfExtractor {

    private String mainPageUrl = "https://allinpdf.com/convert/fileconvert/fileconvert-start";
    private String selectFormatUrl = "https://allinpdf.com/convert/fileconvert/fileconvert-select";
    private String convertFileCompleteUrl = "https://allinpdf.com/convert/fileconvert/fileconvert-complete";
    // chrome options
    private ChromeOptions options;
    private WebDriver webDriver;
    private JavascriptExecutor javascriptExecutor;
    private int max_Attempt = 15;

    //@Value("")
    private String driverPath;

    public HwpPdfExtractor() { }

    //@PostConstruct
    public void startBrowser() {
        try {
            log.info("Start Browser");
            this.options = new ChromeOptions();
            this.options.addArguments("start-maximized");
            System.setProperty("webdriver.chrome.driver","C:\\driver\\chromedriver.exe");
            this.webDriver = new ChromeDriver(this.options);
            this.javascriptExecutor = (JavascriptExecutor) this.webDriver;
        } catch (Exception ex) {
            this.stopBrowser();
            throw new NotFoundException("Driver Not Found Exception " + ex);
        }
    }

    // this method use to convert the hwp file to pdf and get the pdf file url through crawling
    // note d't remove the sysout from this method
    public String startProcessForUrl(String filePath) {
        log.info("=========================================================");
        log.info("File Path " + filePath);
        String fileUrl = null;
        try {
            this.webDriver.get(this.mainPageUrl);
            Thread.sleep(1000);
            if(this.isTargetPage(this.mainPageUrl)) {
                log.info("Current Url :- " + this.webDriver.getCurrentUrl());
                // upload the hwp local file
                WebElement webElement = this.webDriver.findElement(By.xpath("//*[@id=\"CurrentLang\"]/body/div/input"));
                webElement.sendKeys(filePath);
                System.out.print("Process Selector Convert Type");
                if(pageDialer(this.selectFormatUrl)) {
                    // pdf type select from by defulat it's pdf select so click event and start next process
                    this.webDriver.findElement(By.xpath("//*[@id=\"ContentPlaceHolder1_m_btnStartFileConvert\"]")).click();
                    // next step dialer for next step
                    System.out.println("\nProcess Start Converter");
                    if(pageDialer(this.convertFileCompleteUrl)) {
                        // here just get the url of file and download the file
                        //Get list of web-elements with tagName  - a
                        List<WebElement> allLinks = this.webDriver.findElements(By.xpath("//*[@id=\"container\"]/div[2]/div[1]/div/a[1]"));
                        //Traversing through the list and printing its text along with link address
                        for(WebElement link:allLinks){
                            System.out.println(link.getText() + " - " + link.getAttribute("href"));
                        }
                        fileUrl = "url";
                        this.webDriver.findElement(By.xpath("//*[@id=\"container\"]/div[2]/div[1]/div/a[1]")).click();
                        Thread.sleep(1000);
                        // cancel button
                        this.webDriver.findElement(By.xpath("//*[@id=\"container\"]/div[2]/div[1]/div/a[3]")).click();
                        Thread.sleep(500);
                        System.out.println("Process Pdf Url Complete.");
                    }
                }
            }
        } catch (Exception ex) {
            log.info("Exception :- " + ex.getMessage());
            return null;
        }
        return fileUrl;
    }

    @PreDestroy
    // stop the browser on application end
    public void stopBrowser() {
        log.info("Stop Browser");
        if(this.webDriver != null) { this.webDriver.close(); }
    }

    // download file from url
    public ByteArrayOutputStream downloadFile(String url, ByteArrayOutputStream byteArrayOutputStream) {
        try(InputStream in = new URL(url).openStream()) {
            byteArrayOutputStream.write(IOUtils.toByteArray(in));
            if(in != null) { in.close(); }
        } catch (Exception ex) {
            // zero byte return which mean file not downland from the url
            log.error("Exception File Download Fail. " + ex);
        }
        return byteArrayOutputStream;
    }

    // page dialer if page quick found it's stop else it's attempt 15 time
    private Boolean pageDialer(String targetUrl) throws Exception {
        Boolean isFound = false;
        int attempt = 0;
        while (true) {
            Thread.sleep(500);
            // target url same break
            if(isTargetPage(targetUrl) || (attempt == max_Attempt)) {
                // yes it's found start next step
                isFound = true;
                break;
            }
            // Page Not Found So Stop Process. if need add break point
            System.out.print(".");
            attempt = attempt + 1;
        }
        return isFound;
    }

    // check the current page is real target page or not
    private Boolean isTargetPage(String pageUrl) {
        return pageUrl.equals(this.webDriver.getCurrentUrl()) == true ? true : false;
    }

    public static void main(String args[]) {
        HwpPdfExtractor hwpPdfExtractor = new HwpPdfExtractor();
        hwpPdfExtractor.startBrowser();
    }
}
