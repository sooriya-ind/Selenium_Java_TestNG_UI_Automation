package utils;

import enums.FrameworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyUtils {

    private PropertyUtils() {

    }

    private static Properties property;
    private static final Logger log = LogManager.getLogger(PropertyUtils.class);

    static {
        try (FileInputStream file = new FileInputStream(FrameworkConstants.getUserDirectory() + File.separator + "Configuration.properties")) {
            property = new Properties();
            property.load(file);
        } catch (IOException e) {
            log.error(" !!!! Failed Loading Property Utils ---> {}. !!!! ", e.getMessage());
        }
    }

    public static String getValue(String key) {
        return property.getProperty(key);
    }

}
