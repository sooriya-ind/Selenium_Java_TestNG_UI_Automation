package testCase;

import driver.Driver;
import listeners.EventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ThreadGuard;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import utils.BasePage;
import utils.DataProviderUtils;

import java.lang.reflect.Method;

public class Base extends BasePage {

    public static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final Logger log = LogManager.getLogger(Base.class);

    protected Base() {

    }

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void setDriver(String browserName) {

        // Create instance of Listener Class
        EventListener listener = new EventListener();

        // Attach Listener to Driver
        WebDriver decoratedDriver = new EventFiringDecorator<>(listener).decorate(Driver.initDriver(browserName));
        driverThreadLocal.set(ThreadGuard.protect(decoratedDriver));

    }

    @AfterMethod
    public void analyzer(ITestResult result, Method m) {

        // Check Error Message Displayed & If True Scroll To It For Screen Shot
        try {
            scrollAndMove(getDriver(), getDriver().findElements(By.xpath("//*[contains(@class,'MuiFormHelperText-root Mui-error') or contains(@style,'color: rgb(244, 67, 54);')]")).get(0));
        } catch (Exception e) {
            // Do Nothing
        }

        // Check Whether Test is Failed
        if (result.getStatus() == ITestResult.FAILURE) {

            try {
                DataProviderUtils.excelWriter("Execution Status", "Fail");
                testCase(getDriver(), "Flow Failed", "Fail", "Test Failed", "TestFailure.png");
            } catch (Exception e) {
                log.error("Exception while writing result = {}", e.getMessage());
            }

        }

        // Check Whether Test is Skipped
        if (result.getStatus() == ITestResult.SKIP) {

            try {
                testCase(getDriver(), "Flow Skipped", "Skip", "Test Skipped", "TestSkipped.png");
            } catch (Exception e) {
                log.error("Exception while writing result : {}", e.getMessage());
            }

        }

        // Check & Update Details Test is Passed
        if (result.getStatus() == ITestResult.SUCCESS) {

            try {
                DataProviderUtils.excelWriter("Execution Status", "Pass");
            } catch (Exception e) {
                log.error("Exception while writing result - {}", e.getMessage());
            }

        }

    }

    @AfterMethod
    protected void tearDown() {

        // Quit Driver
        try {
            getDriver().quit();
        } catch (Exception e) {
            log.error(" !!! Exception Occurs At Quit Driver !!! --> {}.", e.getMessage());
        }

        // Remove Thread
        log.info("Removed Thread : {}", Thread.currentThread().getName());
        driverThreadLocal.remove();

        // Flush method writes/updates the test information of your reporter
        extent.flush();

    }

}
