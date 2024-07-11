package testCase;

import org.testng.annotations.Test;
import pages.GoogleSearchPage;

public class GoogleTest extends Base {

    @Test
    public void googleTest() {

        // Initiate driver chrome
        setDriver("Chrome");

        GoogleSearchPage searchPage = new GoogleSearchPage(getDriver());
        searchPage.googleSearch("uneed2no");

    }

}
