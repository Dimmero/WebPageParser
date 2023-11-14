package org.example;

import org.example.entities.BookForGroups;
import org.example.pages.*;
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
    private final BookPage BOOK_PAGE;
    private final Cookies COOKIES;

    public SearchBookByIsbnFeature() throws MalformedURLException, URISyntaxException {
        driver = new SeleniumDriver();
        this.INDEX_PAGE = new IndexPage();
        this.SEARCH_PAGE = new SearchPage();
        this.BOOK_PAGE = new BookPage();
        this.COOKIES = new Cookies();
    }

    public String provideIsbnAndGoToBookPage(String isbn, String webPageUrl) {
        driver.getDriver().get(webPageUrl);
        INDEX_PAGE.sendIsbnAndSubmit(isbn);
        COOKIES.turnOffCookies();
        SEARCH_PAGE.clickBookCard();
        driver.sleepForSomeTime(1);
        return driver.getDriver().getCurrentUrl();
    }

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

    public void addRelatedBooks(GroupTypes group, List<String> hrefs, int limit) throws IOException {
        ArrayList<BookForGroups> books = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            if (hrefs == null) {
                break;
            }
            HtmlParser parser = new HtmlParser(hrefs.get(i), Main.webPageUrl);
            driver.getDriver().get(hrefs.get(i));
            try {
                driver.getShortWait10().pollingEvery(Duration.ofMillis(500)).until(ExpectedConditions.urlToBe(hrefs.get(i)));
            } catch (Exception e) {
                continue;
            }
            BookForGroups book = parser.createBookForGroupsData();
            books.add(book);
        }
        setRelatedBooksForBook(String.valueOf(group), books);
    }

    private void setRelatedBooksForBook(String group, ArrayList<BookForGroups> relatedBooks) {
        switch (group) {
            case "SERIES": {
                Main.book.setBooksOfSeries(relatedBooks);
                break;
            }
            case "AUTHORS": {
                Main.book.setBooksOfAuthor(relatedBooks);
                break;
            }
            case "GENRE": {
                Main.book.setBooksOfGenre(relatedBooks);
                break;
            }
        }
    }
}
