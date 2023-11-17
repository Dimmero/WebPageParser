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
    private final String webPageUrl;
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

    public HtmlParser(String urlSource, String webPageUrl) {
        this.urlSource = urlSource;
        this.webPageUrl = webPageUrl;
    }

    public <T extends BookInterface> T createBookData(Class<T> bookType) {
        try {
            T book = bookType.getDeclaredConstructor().newInstance();
            Document document = Jsoup.connect(urlSource).get();
            Element productInfo = document.selectFirst(this.productInfoCss);
            if (bookType.getName().equals(Book.class.getName())) {
                Element productImage = document.selectFirst(this.productImageCss);
                try {
                    if (productInfo != null && productImage != null) {
                        BookDescription bookDescription = setDescriptionForMainBook(productInfo, productImage);
                        book.initializeMainBook(bookDescription);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (productInfo != null) {
                    BookDescriptionForGroups bookDescriptionForGroups = setDescriptionForSeriesBook(productInfo);
                    book.initializeRelatedBook(bookDescriptionForGroups);
                }
            }
            return book;
        } catch (IOException | NoSuchMethodException e) {
            e.getStackTrace();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private BookDescription setDescriptionForMainBook(Element productInfo, Element productImage) {
        String author = productInfo.select(productAuthorsCss).text().replace("Автор: ", "");
        String title = productInfo.attr(productTitleAttr);
        String publisher = productInfo.attr(productPublisherAttr);
        String series = productInfo.attr(productSeriesAttr);
        String bookId = productInfo.attr(productIdAttr);
        String isbnString = productInfo.select(productIsbnCss).text().replace("ISBN: ", "");
        assert productImage != null;
        String image = Objects.requireNonNull(productImage.selectFirst(productImageAttr)).attr(productImageSrcAttr);
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
        images.add(image);
        return new BookDescription(author, title, publisher, series, bookId, isbns, images);
    }

    private BookDescriptionForGroups setDescriptionForSeriesBook(Element productInfo) {
        String bookId = productInfo.attr(productIdAttr);
        String isbnString = productInfo.select(productIsbnCss).text().replace("ISBN: ", "");
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
        return new BookDescriptionForGroups(bookId, isbns);
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
            HtmlParser parser = new HtmlParser(relatedBooksUrls.get(i), Main.webPageUrl);
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
        try {
            Element sectionUrl = Objects.requireNonNull(document.selectFirst("." + section.name().toLowerCase())).selectFirst("a");
            assert sectionUrl != null;
            urlOfSection = this.webPageUrl + sectionUrl.attr("href");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return urlOfSection;
    }

    private ArrayList<String> extractUrlOfSection(Elements section) {
        ArrayList<String> arr = new ArrayList<>();
        for (Element el : section) {
            String href = el.attr("href");
            arr.add(this.webPageUrl + href);
        }
        return new ArrayList<>(arr);
    }
}
