package enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import utils.PropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

public final class FrameworkConstants {

    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String RESOURCES_PATH = FrameworkConstants.USER_DIR + "\\src\\main\\resources";
    public static final String TIMESTAMP = new SimpleDateFormat("dd_MM_yyyy.HH.mm.ss").format(new Date());
    public static final String RESULT_DIR = FrameworkConstants.USER_DIR + File.separator + "Test-Results";
    private static final String CLONED_EXCEL_NAME = new SimpleDateFormat("dd_MM_yyyy.HH.mm.ss").format(new Date()) + ".xlsx";
    private static final String EXTENT_REPORT_PATH = RESULT_DIR + File.separator + TIMESTAMP + ".html";
    private static final String SCREENSHOTS_PATH = RESULT_DIR + File.separator + "screen-shots";
    private static final String LOG_FILE_PATH = FrameworkConstants.USER_DIR + "\\logs\\log4j2.log";
    private static final String DOWNLOADS_PATH = RESULT_DIR + File.separator + "Documents";
    private static final String OLD_RESULTS_PATH = USER_DIR + File.separator + "oldResults";
    private static final String INPUT_EXCEL_LOCATION = "inputExcelLocation";

    private FrameworkConstants() {

    }

    public static String getUserDirectory() {
        return USER_DIR;
    }

    public static String getInputExcelPath() {
        if (PropertyUtils.getValue(INPUT_EXCEL_LOCATION).equalsIgnoreCase("")) {
            return RESOURCES_PATH + File.separator + "TestData.xlsx";
        } else {
            return PropertyUtils.getValue(INPUT_EXCEL_LOCATION);
        }
    }

    public static String getResultDirectoryPath() {
        return RESULT_DIR;
    }

    public static String getClonedExcelName() {
        String excelName = "Berkley_";
        if (PropertyUtils.getValue("url").contains("qa")) {
            excelName = excelName.concat("QA");
        } else if (PropertyUtils.getValue("url").contains("uat")) {
            excelName = excelName.concat("UAT");
        } else {
            excelName = excelName.concat("UCI");
        }
        return excelName.concat("_" + PropertyUtils.getValue("lob") + "_" + CLONED_EXCEL_NAME);
    }

    public static String getExtentReportPath() {
        return EXTENT_REPORT_PATH;
    }

    public static String getScreenShotsPath() {
        return SCREENSHOTS_PATH;
    }

    public static String getLogFilePath() {
        return LOG_FILE_PATH;
    }

    public static String getDownloadsPath() {
        return DOWNLOADS_PATH;
    }

    public static String getOldResultsPath() {
        return OLD_RESULTS_PATH;
    }

}
