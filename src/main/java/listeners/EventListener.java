package listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

public class EventListener implements WebDriverListener {

    private static final Logger log = LogManager.getLogger(EventListener.class);

    @Override
    public void afterClick(WebElement element) {

        // To Get Name Of The Element & Log In Console
        try {
            if (element.getAccessibleName().equalsIgnoreCase("")) {
                log.info("{} is  clicked successfully.", element);
            } else {
                log.info("{} is clicked successfully", element.getAccessibleName());
            }
        } catch (Exception e) {
            log.info("{}  is clicked successfully.", element);
        }

    }

    @Override
    public void afterSendKeys(WebElement element, CharSequence... keysToSend) {

        // To Get Name Of The Element & Log In Console
        try {
            if (!element.getText().isEmpty() || !element.getAttribute("value").isEmpty()) {
                log.info("{} is clicked & entered {} successfully.", element.getAccessibleName(), keysToSend);
            } else {
                log.warn(" Unsuccessful upon entering {} to this {}", keysToSend, element.getAccessibleName());
            }
        } catch (Exception e) {
            log.warn("Unsuccessful upon entering {} to this {}", keysToSend, element);
        }

    }

}
