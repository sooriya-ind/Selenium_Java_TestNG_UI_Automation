package driver;

import io.restassured.path.json.JsonPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v123.network.Network;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import utils.DataProviderUtils;
import utils.PropertyUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class Driver {

    private Driver() {

    }

    private static final Logger log = LogManager.getLogger(Driver.class);
    private static final String CHROME = "chrome";
    private static final String EDGE = "edge";
    private static final String SUBMISSION_TYPE = "SubmissionType";

    public static WebDriver initDriver(String browserType) {

        // Initializing Driver & Setting Variables
        WebDriver driver = null;
        DevTools devTools = null;
        ChromeOptions chromeOptions = null;
        EdgeOptions edgeOptions = null;

        // Setting Up Desired Capabilities
        DesiredCapabilities cap = new DesiredCapabilities();

        // Creating Browser Instances [ Chrome || Edge ] used to Set up Driver Dynamically
        if (browserType.equalsIgnoreCase(CHROME)) {

            // Initiating the chromedriver
            chromeOptions = OptionsManager.getChromeOptions();
            cap.setBrowserName(CHROME);
            cap.setPlatform(Platform.WIN10);
            cap.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        } else if (browserType.equalsIgnoreCase(EDGE)) {

            // Initiating the Edge Driver
            edgeOptions = OptionsManager.getEdgeOptions();
            cap.setBrowserName(EDGE);
            cap.setPlatform(Platform.WIN10);
            cap.setCapability(EdgeOptions.CAPABILITY, edgeOptions);

        } else {

            log.error(" Wrong Driver ");

        }

        // Scaling Browser Factory to run in Selenium GRID & also Local Environment
        if (PropertyUtils.getValue("execution").equalsIgnoreCase("Grid")) {

            try {

                driver = (new RemoteWebDriver(new URL(PropertyUtils.getValue("gridPort")), cap));

            } catch (MalformedURLException e) {

                log.error(" !!! Problem connecting with GRID !!! ");

            }

        } else {

            if (browserType.equalsIgnoreCase(CHROME)) {

                assert chromeOptions != null;
                driver = new ChromeDriver(chromeOptions);
                devTools = ((ChromeDriver) driver).getDevTools();

            } else if (browserType.equalsIgnoreCase(EDGE)) {

                assert edgeOptions != null;
                driver = new EdgeDriver(edgeOptions);
                devTools = ((EdgeDriver) driver).getDevTools();

            } else {

                log.error(" !!! Given Browser Type Is Not Supported : {} !!! ", browserType);

            }

            // Start To Listen Networks
//            assert devTools != null;
//            networkListeners(devTools);

        }

        // Add page load timeout
        assert driver != null;
        driver.manage()
                .timeouts()
                .pageLoadTimeout(Duration.ofSeconds(120));

        return driver;

    }

    public static void networkListeners(DevTools devTools) {

        // Create a map to store the start time for each request with matching eventName
        ConcurrentHashMap<String, HashMap<String, Object>> startTimeMap = new ConcurrentHashMap<>();

        // Create DevTools To Listen Networks
        devTools.createSession();
        devTools.send(Network.enable(Optional.of(1000000), Optional.empty(), Optional.empty()));
        devTools.addListener(Network.requestWillBeSent(), request -> {

            String reqBody = request.getRequest().getPostData().toString().substring(9, request.getRequest().getPostData().toString().length() - 1);
            JsonPath jsonPath = new JsonPath(reqBody);
            String eventName; // Get the EventName from the request JSON
            String targetEventName; // Get the TargetEventName from the request JSON
            String submissionType; // Get the TargetEventName from the request JSON

            try {

                eventName = jsonPath.get("EventName");
                targetEventName = jsonPath.get("TargetEventName");
                submissionType = jsonPath.get(SUBMISSION_TYPE);

                // Get Start Time & Store
                if (eventName.contains("SolartisMasterWF") && (targetEventName.contains("CalculateExperienceFactorAndRateWithEmail_WF_V1"))) { // Check if the event name matches

                    // Create Hash Map To Store Request Type
                    HashMap<String, Object> innerMap = new HashMap<>();
                    innerMap.put("Instant", Instant.now());
                    innerMap.put(SUBMISSION_TYPE, submissionType);
                    startTimeMap.put(String.valueOf(request.getRequestId()), innerMap);

                    log.info("Request Method : {}", request.getRequest().getMethod());
                    log.info("Request URL : {}", request.getRequest().getUrl());
                    log.info("Request headers: {}", request.getRequest().getHeaders());
                    log.info("Request body: {}", reqBody);

                }

            } catch (Exception e) {
                // Do Nothing To Ignore Json Exceptions
            }

        });

        // Add a listener for the responseReceived event to calculate and log the response time
        devTools.addListener(Network.responseReceived(), response -> {
            String requestId = String.valueOf(response.getRequestId());
            if (startTimeMap.containsKey(requestId)) {
                HashMap<String, Object> innerMap = startTimeMap.get(requestId);
                Instant startTime = (Instant) innerMap.get("Instant");
                Instant endTime = Instant.now();
                Duration responseTime = Duration.between(startTime, endTime);
                log.info("Response Time for request {}: {} Minutes {} Seconds {} Millis.", requestId, responseTime.toMinutesPart(), responseTime.toSecondsPart(), responseTime.toMillisPart());
                String resTime = responseTime.toMinutesPart() + " Minutes " + responseTime.toSecondsPart() + " Seconds " + responseTime.toMillisPart() + " Millis";
                if (innerMap.get(SUBMISSION_TYPE).equals("NEW-BUSINESS")) {
                    DataProviderUtils.excelWriter("New Business Rate Time", resTime, "0");
                } else if (innerMap.get(SUBMISSION_TYPE).equals("ENDORSEMENT")) {
                    DataProviderUtils.excelWriter("Endorsement Rate Time", resTime, "1");
                } else if (innerMap.get(SUBMISSION_TYPE).equals("RENEWAL")) {
                    DataProviderUtils.excelWriter("Renewal Rate Time", resTime, "2");
                }
            }
        });

    }

}
