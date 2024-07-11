package pages;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utils.BasePage;
import utils.PropertyUtils;

import java.time.Duration;

public class GoogleSearchPage extends BasePage {

    WebDriver driver;

    @FindBy(name = "q")
    WebElement txtSearch;

    public GoogleSearchPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void googleSearch(String searchText) {

        // load url
        driver.get(PropertyUtils.getValue("url"));

        // Wait until clickable & Enter in search bar
        waitToBe(driver, waitToBe.CLICKABLE, "MIN", 2, txtSearch);
        customClick(driver, txtSearch, searchText);

        // Hit Enter & wait 5 secs
        actions(driver).sendKeys(Keys.ENTER).pause(Duration.ofSeconds(5)).perform();

    }

}
