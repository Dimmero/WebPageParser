package parser;

import parser.entities.BookForGroups;
import parser.pages.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SearchBookByIsbnFeature extends BaseAbstractPage {
    private final IndexPage INDEX_PAGE;
    private final SearchPage SEARCH_PAGE;
    private final Cookies COOKIES;

    public SearchBookByIsbnFeature() throws MalformedURLException, URISyntaxException {
        driver = new SeleniumDriver();
        this.INDEX_PAGE = new IndexPage();
        this.SEARCH_PAGE = new SearchPage();
        this.COOKIES = new Cookies();
    }

    public String provideIsbnAndGoToBookPage(String isbn, String webPageUrl) {
        driver.getDriver().get(webPageUrl);
        COOKIES.turnOffCookies();
        INDEX_PAGE.sendIsbnAndSubmit(isbn);
        SEARCH_PAGE.clickBookCard();
        driver.sleepForSomeTime(1);
        return driver.getDriver().getCurrentUrl();
    }

    //in case you need to get some info (book availability) you need to split the method provideIsbnAndGoToBookPage()
    // into 2 methods. Search a book, get some info and then continueAndGoToBookPage()

    public String continueAndGoToBookPage() {
        SEARCH_PAGE.clickBookCard();
        driver.sleepForSomeTime(1);
        return driver.getDriver().getCurrentUrl();
    }

    public boolean getInfoAboutBookAvailability() {
        try {
            String outOfStock = SEARCH_PAGE.getOutOfStockInfo();
            if (!outOfStock.isBlank()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
