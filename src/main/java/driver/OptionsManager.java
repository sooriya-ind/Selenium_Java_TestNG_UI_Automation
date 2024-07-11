package driver;

import enums.FrameworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import utils.PropertyUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

public final class OptionsManager {

    private OptionsManager() {
    }

    private static final String START_MAX = "start-maximized";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String BROWSER_VERSION = "browserVersion";
    private static final String HEADLESS = "--headless";
    private static final Logger log = LogManager.getLogger(OptionsManager.class);

    public static ChromeOptions getChromeOptions() {

        System.setProperty("webdriver.chrome.silentOutput", "true");

        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory", FrameworkConstants.getDownloadsPath());
        chromePrefs.put("plugins.always_open_pdf_externally", true);

        ChromeOptions options = new ChromeOptions();
        options.addArguments(START_MAX);
        options.addArguments("--remote-allow-origins=*");
        options.setExperimentalOption("prefs", chromePrefs);

        if (!PropertyUtils.getValue(BROWSER_VERSION).isEmpty()) {
            options.setBrowserVersion(PropertyUtils.getValue(BROWSER_VERSION));
        }

        if (PropertyUtils.getValue("headless").equalsIgnoreCase("True")) {
            int width = getScreenDimension(WIDTH);
            int height = getScreenDimension(HEIGHT);

            if (width > 0 && height > 0) {
                log.info("Current Screen Resolution (Width, Height): {}, {}", width, height);
                options.addArguments(HEADLESS);
                options.addArguments("window-size=" + width + "," + height);
            } else {
                log.error("Failed to get screen resolution. Defaulting to headless mode without window size.");
                options.addArguments(HEADLESS);
            }
        }

        return options;
    }

    public static EdgeOptions getEdgeOptions() {

        HashMap<String, Object> edgePrefs = new HashMap<>();
        edgePrefs.put("download.default_directory", FrameworkConstants.getDownloadsPath());
        edgePrefs.put("plugins.always_open_pdf_externally", true);

        EdgeOptions options = new EdgeOptions();
        options.addArguments(START_MAX);
        options.setExperimentalOption("prefs", edgePrefs);

        if (!PropertyUtils.getValue(BROWSER_VERSION).isEmpty()) {
            options.setBrowserVersion(PropertyUtils.getValue(BROWSER_VERSION));
        }

        if (PropertyUtils.getValue("headless").equalsIgnoreCase("True")) {
            int width = getScreenDimension(WIDTH);
            int height = getScreenDimension(HEIGHT);

            if (width > 0 && height > 0) {
                log.info("Current Screen Resolution (Width, Height) - {}, {}", width, height);
                options.addArguments(HEADLESS);
                options.addArguments("window-size=" + width + "," + height);
            } else {
                log.error("Failed to get screen resolution, Defaulting to headless mode without window size.");
                options.addArguments(HEADLESS);
            }
        }

        return options;
    }

    private static int getScreenDimension(String dimension) {
        String command = null;

        if (dimension.equalsIgnoreCase(HEIGHT)) {
            command = "powershell.exe wmic path Win32_VideoController get CurrentVerticalResolution";
        } else if (dimension.equalsIgnoreCase(WIDTH)) {
            command = "powershell.exe wmic path Win32_VideoController get CurrentHorizontalResolution";
        }

        try {
            Process powerShellProcess = Runtime.getRuntime().exec(command);
            powerShellProcess.getOutputStream().close();

            BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
            List<String> output = stdout.lines().toList();
            stdout.close();

            if (output.size() > 2) {
                return Integer.parseInt(output.get(2).strip());
            } else {
                log.error("Unexpected output size: {}", output.size());
            }
        } catch (IOException | NumberFormatException e) {
            log.error("Failed to get screen resolution: {}", e.getMessage());
        }

        return -1;
    }

}
