package com.raad.converter.automate;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;


public class EnvironmentManager {

    private static final String SANDBOK = "--no-sandbox";
    private static final String CERTIFICATE_ERROR = "--ignore-certificate-errors";
    private static final String START_MAXIMIZED = "--start-maximized";
    private static final String DISABLE_DEV_SHM_USAGE = "--disable-dev-shm-usage";
    private static final String INCONGNITO = "--incognito";
    private static final String HEADLESS = "--headless";
    private static final String START_FULL_SCREEN = "start-fullscreen";

    public EnvironmentManager() {}

    // we start with chrome-driver
    public static void initWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(SANDBOK);
        options.addArguments(CERTIFICATE_ERROR);
        options.addArguments(START_MAXIMIZED);
        options.addArguments(DISABLE_DEV_SHM_USAGE);
        options.addArguments(INCONGNITO);
        //options.addArguments(HEADLESS);
        options.addArguments(START_FULL_SCREEN);
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        //System.setProperty("webdriver.chrome.driver", "C:\\Users\\Nabeel.Ahmed\\Downloads\\RAAD-Converter\\driver\\chromedriver.exe");
        RunEnvironment.setWebDriver(new ChromeDriver(capabilities));
    }

    // stop the driver
    public static void shutDownDriver() {
        RunEnvironment.getWebDriver().close();
    }

}