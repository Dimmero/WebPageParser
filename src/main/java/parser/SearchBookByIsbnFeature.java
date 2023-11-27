package parser;

import org.openqa.selenium.WebElement;
import parser.pages.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

    public List<String> provideIsbnAndGoToBookPage(String isbn, String webPageUrl) {
        driver.getDriver().get(webPageUrl);
//        COOKIES.turnOffCookies();
        INDEX_PAGE.sendIsbnAndSubmit(isbn);
        List<String> cards = new ArrayList<>();
        for (WebElement el: SEARCH_PAGE.findProductCards()) {
            cards.add(el.getAttribute("href"));
        }
        return cards;
    }

    //in case you need to get some info (book availability) you need to split the method provideIsbnAndGoToBookPage()
    // into 2 methods. Search a book, get some info and then continueAndGoToBookPage()

    public String continueAndGoToBookPage() {
//        SEARCH_PAGE.clickBookCard();
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
