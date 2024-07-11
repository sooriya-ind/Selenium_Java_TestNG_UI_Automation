package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import enums.FrameworkConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.asserts.SoftAssert;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasePage {

    private static final Logger log = LogManager.getLogger(BasePage.class);
    public static final ExtentReports extent = new ExtentReports();
    private static final String VALUE = "value";

    public ExtentTest test;

    public enum waitToBe {
        VISIBLE,
        CLICKABLE,
        INVISIBLE
    }

    @BeforeSuite
    public void preRequisites() {

        // Creating a File object for directory
        File directoryPath = new File(FrameworkConstants.getResultDirectoryPath());

        // Get TimeStamp From Excel Output File
        String folderName = null;
        File[] listOfFiles = directoryPath.listFiles();
        try {
            assert listOfFiles != null;
        } catch (AssertionError e) {
            try {
                FileUtils.forceMkdir(directoryPath);
                listOfFiles = directoryPath.listFiles();
            } catch (IOException ex) {
                log.error(" !!! Fail Safe 'test-output' folder creation failed. !!! ");
            }
        }
        assert listOfFiles != null;
        for (File fileName : listOfFiles) {
            if (fileName.isFile() && fileName.getName().contains(".xlsx")) {
                folderName = fileName.getName().replace("TestOutput_", "").replace(".xlsx", "");
            }
        }

        // List of all files and directories
        for (File file : listOfFiles) {

            try {

                if (file.isFile()) {

                    FileUtils.moveFileToDirectory(file, new File(FrameworkConstants.getOldResultsPath() + File.separator + folderName), true);

                } else if (file.isDirectory()) {

                    FileUtils.moveDirectoryToDirectory(file, new File(FrameworkConstants.getOldResultsPath() + File.separator + folderName), true);

                }
            } catch (IOException ex) {
                log.error("!!! Moving Old Reports Failed !!!");
            }

        }

        // Clear Log File Contents
        try {
            new FileWriter(FrameworkConstants.getLogFilePath(), false).close();
            log.info("Cleared Old Logs...");
        } catch (IOException e) {
            log.error("!!! Clear Old Logs Failed !!!");
        }

        preRequisitesHelper();

    }

    private void preRequisitesHelper() {

        // Create Directories For Test
        try {
            FileUtils.forceMkdir(new File(FrameworkConstants.getResultDirectoryPath() + "\\screen-shots"));
            FileUtils.forceMkdir(new File(FrameworkConstants.getResultDirectoryPath() + "\\imports"));
            log.info("Created Output Directory...");
        } catch (IOException e) {
            log.error("Create directory with screen-shots folder failed.");
        }

        // Extent Reports V5 - Spark Reporter Initialization & Directory where output is to be printed
        ExtentSparkReporter spark = new ExtentSparkReporter(FrameworkConstants.getExtentReportPath());

        // Configure Spark Reporter
        try {
            spark.loadXMLConfig(FrameworkConstants.getUserDirectory() + "\\ReportsConfig.xml");
            extent.setSystemInfo("User Name", System.getProperty("user.name"));
            extent.setSystemInfo("Time Zone", System.getProperty("user.timezone"));
            extent.setSystemInfo("Machine", "Windows 10" + "64 Bit");
            extent.setSystemInfo("Selenium", "4.10.0");
            extent.setSystemInfo("Maven", "3.8.8");
            extent.setSystemInfo("Java Version", "Open JDK 17.0.2");
            log.info("Extent Spark Report Configuration Completed...");
        } catch (IOException e) {
            log.error("!!! Loading Extent Spark Reporter XML Configuration Failed !!! ---> {}", e.getMessage());
        }
        spark.viewConfigurer().
                viewOrder().
                as(new ViewName[]{ViewName.DASHBOARD, ViewName.CATEGORY, ViewName.TEST, ViewName.AUTHOR}).
                apply();

        // Attaching Reporters to Extend Report
        extent.attachReporter(spark);

    }

    @AfterSuite
    public void moveLog() {

        // Create a copy & Move Log file to Results Folder
        try {
            FileUtils.copyFile(new File(FrameworkConstants.getLogFilePath()), new File(FrameworkConstants.getResultDirectoryPath() + "\\log4j2.log"));
            log.info("Log File Moved Successfully.");
        } catch (IOException e) {
            log.error("!!! Move Log To Results Folder Failed !!!");
        }

    }

    private void clear(WebElement element) {

        // Used to Send back button until last character disappears
        while (element.isDisplayed() && !element.getAttribute(VALUE).isEmpty()) {
            element.sendKeys(Keys.chord(Keys.CONTROL, Keys.getKeyFromUnicode('A'), Keys.BACK_SPACE));
        }

    }

    public void selectDate(WebDriver driver, WebElement datePicker, String date) {

        // Get the current value of the date picker
        String screenDate = datePicker.getAttribute(VALUE);

        // Handling exception if the date picker field does not contain any value
        if (screenDate == null || screenDate.isEmpty()) {
            screenDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
        }

        String[] screenSplitter = screenDate.split("/");

        if (screenSplitter.length != 3) {
            throw new IllegalArgumentException("Invalid date format in date picker");
        }

        String ssMonth = screenSplitter[0];
        String ssYear = screenSplitter[2];

        String[] inputSplitter = date.split("/");

        if (inputSplitter.length != 3) {
            throw new IllegalArgumentException("Invalid date format in input date");
        }

        String inMonth = inputSplitter[0];
        String inDate = inputSplitter[1];
        String inYear = inputSplitter[2];

        if (inMonth.startsWith("0")) {
            inMonth = String.valueOf(inMonth.charAt(1));
        }

        if (inDate.startsWith("0")) {
            inDate = String.valueOf(inDate.charAt(1));
        }

        // Year
        if (!ssYear.equals(inYear)) {
            loaderValidator(driver);
            customClick(driver, driver.findElement(By.xpath("//h6[normalize-space()='" + ssYear + "']")));
            customClick(driver, driver.findElement(By.xpath("//div[normalize-space()='" + inYear + "']")));
        }

        // Month
        int diff = Integer.parseInt(ssMonth) - Integer.parseInt(inMonth);
        if (diff < 0) {
            for (int i = diff; i < 0; i++) {
                loaderValidator(driver);
                WebElement btnForwards = driver.findElement(By.xpath("//div[@class='MuiPickersCalendarHeader-switchHeader']//button[2]"));
                customClick(driver, btnForwards);
            }
        } else if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                loaderValidator(driver);
                WebElement btnBackwards = driver.findElement(By.xpath("//div[@class='MuiPickersCalendarHeader-switchHeader']//button[1]"));
                customClick(driver, btnBackwards);
            }
        }

        // Date
        loaderValidator(driver);
        customClick(driver, driver.findElement(By.xpath("//button[not(contains(@class, 'hidden'))]//p[normalize-space()='" + inDate + "']")));

    }

    public void scrollAndMove(WebDriver driver, WebElement element) {

        // Scroll & Move To The Element - If Exception Occurs Wait & Click
        loaderValidator(driver);
        try {
            actions(driver)
                    .scrollToElement(element)
                    .moveToElement(element)
                    .perform();
        } catch (Exception e) {
            actions(driver)
                    .pause(Duration.ofSeconds(5))
                    .scrollToElement(element)
                    .moveToElement(element)
                    .perform();
        }

    }

    public void customClick(WebDriver driver, WebElement element) {

        // To Check Loader & Validate
        loaderValidator(driver);

        // Scroll & Move To The Element
        scrollAndMove(driver, element);

        // Click The Element
        try {
            element.click();
        } catch (Exception e) {
            actions(driver).click(element).perform();
        }

    }

    public void selectDropDown(WebDriver driver, String option) {

        // To Check Loader & Validate
        loaderValidator(driver);

        // Dynamic String Manipulator
        String str = option.contains("\"") ? "'" : "\"";

        // Set Drop Down Value
        WebElement element;
        actions(driver).pause(Duration.ofMillis(500)).perform();
        try {
            element = driver.findElement(By.xpath("//li[@data-value=" + str + option + str + "]"));
        } catch (Exception e) {
            element = driver.findElement(By.xpath("//li[text()=" + str + option + str + "]"));
        }

        // Select Option
        customClick(driver, element);

    }

    public void customClick(WebDriver driver, WebElement element, String text) {

        // To Check Loader & Validate
        loaderValidator(driver);

        // To Check The Element Have Any Existing Text & Delete
        try {
            if (!element.getText().isEmpty() || !element.getAttribute(VALUE).isEmpty()) {
                clear(element);
            }
        } catch (Exception e) {
            log.warn(" !!! Check The Element Have Any Existing Text & Delete Failed !!! ");
        }

        // Scroll & Move To The Element
        scrollAndMove(driver, element);

        // Remove Any Spaces Before & After Then Enter Keys To The Element
        String textToEnter = text.strip();
        element.sendKeys(textToEnter);

    }

    public static Actions actions(WebDriver driverReference) {

        return new Actions(driverReference);

    }

    public void waitToBeClickable(WebDriver driver, WebElement element) {

        // Check For Loader & Wait for the Element to be Clickable
        loaderValidator(driver);
        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofMinutes(5))
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
        wait.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(element)));
    }

    public void waitToBeVisible(WebDriver driver, WebElement element) {

        // Check For Loader & Wait for the Element to be Clickable
        loaderValidator(driver);
        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofMinutes(5))
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
        wait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(element)));
    }

    /**
     * This Method Is Created To Reuse Multiple Places - Use Below Scenario's To Use This Method Efficiently
     *
     * @param waitScenario "SEC" --> Seconds, "MIN" --> Minutes, "HRS" --> Hours, "DAY" --> Days
     */
    public void waitToBeClickable(WebDriver driver, String waitScenario, int time, WebElement element) {

        // Check For Loader & Wait for the Element to be Clickable
        loaderValidator(driver);
        WebDriverWait wait;

        switch (waitScenario.toUpperCase()) {
            case "MIN" -> wait = new WebDriverWait(driver, Duration.ofMinutes(time));
            case "HRS" -> wait = new WebDriverWait(driver, Duration.ofHours(time));
            case "DAY" -> wait = new WebDriverWait(driver, Duration.ofDays(time));
            default -> wait = new WebDriverWait(driver, Duration.ofSeconds(time));
        }

        wait.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(element)));
    }

    /**
     * This Method Is Created To Reuse Multiple Places - Use Below Scenario's To Use This Method Efficiently
     *
     * @param waitScenario "SEC" --> Seconds, "MIN" --> Minutes, "HRS" --> Hours, "DAY" --> Days
     */
    public void waitToBeVisible(WebDriver driver, String waitScenario, int time, WebElement element) {

        // Check For Loader & Wait for the Element to be Clickable
        loaderValidator(driver);
        WebDriverWait wait;

        switch (waitScenario.toUpperCase()) {
            case "MIN" -> wait = new WebDriverWait(driver, Duration.ofMinutes(time));
            case "HRS" -> wait = new WebDriverWait(driver, Duration.ofHours(time));
            case "DAY" -> wait = new WebDriverWait(driver, Duration.ofDays(time));
            default -> wait = new WebDriverWait(driver, Duration.ofSeconds(time));
        }

        wait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(element)));
    }

    /**
     * This Method Is Created To Reuse Multiple Places - Use Below Scenario's To Use This Method Efficiently
     *
     * @param waitScenario "SEC" --> Seconds, "MIN" --> Minutes, "HRS" --> Hours, "DAY" --> Days
     */
    public void waitToBe(WebDriver driver, waitToBe waitCondition, String waitScenario, int time, WebElement element) {

        // Check For Loader & Wait for the Element to be Clickable
        loaderValidator(driver);
        WebDriverWait wait;

        switch (waitScenario.toUpperCase()) {
            case "MIN" -> wait = new WebDriverWait(driver, Duration.ofMinutes(time));
            case "HRS" -> wait = new WebDriverWait(driver, Duration.ofHours(time));
            case "DAY" -> wait = new WebDriverWait(driver, Duration.ofDays(time));
            default -> wait = new WebDriverWait(driver, Duration.ofSeconds(time));
        }

        switch (waitCondition) {
            case VISIBLE -> wait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(element)));
            case CLICKABLE ->
                    wait.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(element)));
            case INVISIBLE -> wait.until(ExpectedConditions.refreshed(ExpectedConditions.invisibilityOf(element)));
        }

    }

    public void loaderValidator(WebDriver driver, String... customLoader) {

        // To Use In Various Websites
        String loader = customLoader.length == 0 ? "//div[@class='custom-loader-container']" : customLoader[0];

        // Check Loader Is Present - If True then Validate
        if (isLoaderPresent(driver, loader)) {

            // Wait Condition To Check The Loader Is In The Screen Or Not
            FluentWait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofMinutes(5))
                    .pollingEvery(Duration.ofMillis(250))
                    .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

            wait.until(ExpectedConditions.refreshed(ExpectedConditions.invisibilityOfElementLocated(By.xpath(loader))));

        }

    }

    public boolean isLoaderPresent(WebDriver driver, String... loader) {

        // To Use In Various Websites
        String loaderXpath = loader.length == 0 ? "//div[@class='custom-loader-container']" : loader[0];

        try {
            return driver.findElement(By.xpath(loaderXpath)).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }

    }

    public void testCase(WebDriver driver, String testName, String status, String description, String imagePath) {

        // Loader Validation Added To Make Sure It Does not Capture Loader
        try {
            loaderValidator(driver);
        } catch (Exception e) {
            log.error(" !!! Loading More Than Expected.....................................................");
        }

        // Initiating New Test with the Given Name & Set Author Name
        test = extent.createTest(Thread.currentThread().getName() + "_" + testName);
        test.assignCategory("Iteration_" + Thread.currentThread().getName());
        test.assignAuthor("Sooriya_G");

        // Initiating driver to take screenshot Entire Page
        TakesScreenshot ts = (TakesScreenshot) driver;

        // Converting image path according to the Iteration
        String[] inputSplitter = imagePath.split("\\."); // Splits "Login.png" ==> "Login" & "png"
        String fileName = inputSplitter[0];
        String extensionFormat = inputSplitter[1];

        // Below code usage ===> OutputPath = 1_Login.png | Old --> Login_1.png
        String outputPath = Thread.currentThread().getName() + "_" + fileName + "." + extensionFormat;

        // Taking & Moving the Screenshot to the specific location
        try {
            if (PropertyUtils.getValue("ssMethod").equalsIgnoreCase("Robot")) {
                BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ImageIO.write(image, "png", new File(FrameworkConstants.getScreenShotsPath() + File.separator + outputPath));
            } else {
                File source = ts.getScreenshotAs(OutputType.FILE);
                File target = new File(FrameworkConstants.getScreenShotsPath() + File.separator + outputPath);
                FileUtils.copyFile(source, target);
            }
            log.info("Screenshot saved successfully at {}", outputPath);
        } catch (AWTException | IOException e) {
            log.error("Screen Shot Failed", e);
        }

        // Switch case for all scenarios
        String screenShotPath = "screen-shots/";
        switch (status.toLowerCase()) {
            case "pass" -> test.pass(description).addScreenCaptureFromPath(screenShotPath + outputPath);
            case "fail" -> test.fail(description).addScreenCaptureFromPath(screenShotPath + outputPath);
            case "info" -> test.info(description).addScreenCaptureFromPath(screenShotPath + outputPath);
            case "skip" -> test.skip(description).addScreenCaptureFromPath(screenShotPath + outputPath);
            default -> log.warn(" !!! Please check the input !!! ");
        }

    }

}
