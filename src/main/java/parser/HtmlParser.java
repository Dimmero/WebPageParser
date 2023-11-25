package parser;

import parser.entities.*;
import parser.pages.BaseAbstractPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HtmlParser extends BaseAbstractPage {
    private final String urlSource;
    private final String productInfoCss = "#product-info";
    private final String productImageCss = "#product-image";
    private final String productAuthorsCss = ".authors";
    private final String productTitleAttr = "data-name";
    private final String productPublisherAttr = "data-pubhouse";
    private final String productSeriesAttr = "data-series";
    private final String productIdAttr = "data-product-id";
    private final String productIsbnCss = ".isbn";
    private final String productImageAttr = "img";
    private final String productImageSrcAttr = "data-src";
    private final String elementOfSection = "a.cover";
    private final String productAnnotationCss = "#product-about";

    public HtmlParser(String urlSource) {
        this.urlSource = urlSource;
    }

    public <T extends BookInterface> T createBookData(Class<T> bookType) {
        try {
            T book = bookType.getDeclaredConstructor().newInstance();
            Document document = Jsoup.connect(urlSource).get();
            if (bookType.getName().equals(Book.class.getName())) {
                BookDescription bookDescription = setDescriptionForBook(document, BookDescription.class);
                book.initializeMainBook(bookDescription);
            } else {
                BookDescriptionForGroups bookDescriptionForGroups = setDescriptionForBook(document, BookDescriptionForGroups.class);
                book.initializeRelatedBook(bookDescriptionForGroups);
            }
            return book;
        } catch (IOException | NoSuchMethodException e) {
            e.getStackTrace();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private <T extends BookDescriptionInterface> T setDescriptionForBook(Document document, Class<T> descriptionType) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T bookDescription = descriptionType.getDeclaredConstructor().newInstance();
        Element productInfo = document.selectFirst(this.productInfoCss);
        assert productInfo != null;
        String bookId = productInfo.attr(productIdAttr);
        String isbnString = productInfo.select(productIsbnCss).text().replace("ISBN: ", "");
        ArrayList<String> images = new ArrayList<>();
        String[] arrayOfIsbns = isbnString.split(",");
        for (int i = 0; i < arrayOfIsbns.length; i++) {
            if (i == 0) {
                arrayOfIsbns[i] = arrayOfIsbns[i].replace("все", "");
            }
            if (i == arrayOfIsbns.length - 1) {
                arrayOfIsbns[i] = arrayOfIsbns[i].replace("скрыть", "");
            }
            arrayOfIsbns[i] = arrayOfIsbns[i].replace("-", "").trim();
        }
        ArrayList<String> isbns = new ArrayList<>(Arrays.asList(arrayOfIsbns));
        String image;
        if (descriptionType.getName().equals(BookDescription.class.getName())) {
            String author = productInfo.select(productAuthorsCss).text().replace("Автор: ", "");
            String title = productInfo.attr(productTitleAttr);
            String annotation = document.select(productAnnotationCss).select("p").text();
            String publisher = productInfo.attr(productPublisherAttr);
            String series = productInfo.attr(productSeriesAttr);
            Element productImage = document.selectFirst(this.productImageCss);
            assert productImage != null;
            image = Objects.requireNonNull(productImage.selectFirst(productImageAttr)).attr(productImageSrcAttr);
            images.add(image);
            bookDescription.initializeDescriptionForMainBook(author, title, annotation, publisher, series, bookId, isbns, images);
        } else {
            bookDescription.initializeDescriptionForSectionGroup(bookId, isbns);
        }
        return bookDescription;
    }

    public void addRelatedBooks(GroupTypes group, List<String> relatedBooksUrls, int limit) {
        ArrayList<BookForGroups> books = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            if (relatedBooksUrls == null) {
                break;
            }
            if (relatedBooksUrls.size() < limit) {
                limit = relatedBooksUrls.size();
            }
            HtmlParser parser = new HtmlParser(relatedBooksUrls.get(i));
            driver.getDriver().get(relatedBooksUrls.get(i));
            try {
                driver.getShortWait10().pollingEvery(Duration.ofMillis(500)).until(ExpectedConditions.urlContains(relatedBooksUrls.get(i).substring(20)));
            } catch (Exception e) {
                continue;
            }
            BookForGroups book = parser.createBookData(BookForGroups.class);
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

    public List<String> getBooksForNthElementsOfSection(String sectionUrl) {
        List<String> sectionUrls;
        if (sectionUrl.isBlank()) {
            return null;
        }
        driver.getDriver().get(sectionUrl);
        driver.getShortWait10().until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("html")));
        String html = driver.getDriver().getPageSource();
        Document document = Jsoup.parse(html);
        Elements sectionElements = document.select(elementOfSection);
        sectionUrls = extractUrlOfSection(sectionElements);
        return new ArrayList<>(sectionUrls);
    }

    public String getSectionUrl(GroupTypes section, String url) throws IOException {
        driver.getDriver().get(url);
        Document document = Jsoup.connect(url).get();
        String urlOfSection = "";
        String sectionName = section.name().toLowerCase();
        Element sectionUrl;
        try {
            if (section.equals(GroupTypes.GENRE)) {
                sectionUrl = Objects.requireNonNull(document.selectFirst("." + "thermo-item_last")).selectFirst("a");
            } else {
                sectionUrl = Objects.requireNonNull(document.selectFirst("." + sectionName)).selectFirst("a");
            }
            assert sectionUrl != null;
            urlOfSection = Main.webPageUrl + sectionUrl.attr("href");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return urlOfSection;
    }

    private ArrayList<String> extractUrlOfSection(Elements section) {
        ArrayList<String> arr = new ArrayList<>();
        for (Element el : section) {
            String href = el.attr("href");
            arr.add(Main.webPageUrl + href);
        }
        return new ArrayList<>(arr);
    }
}
