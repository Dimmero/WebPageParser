package parser;

import parser.entities.BookDescription;
import parser.entities.BookDescriptionForGroups;
import parser.entities.BookForGroups;
import parser.pages.BaseAbstractPage;
import parser.entities.Book;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HtmlParser extends BaseAbstractPage {
    private final String urlSource;
    private final String webPageUrl;

    public HtmlParser(String urlSource, String webPageUrl) {
        this.urlSource = urlSource;
        this.webPageUrl = webPageUrl;
    }

    public Book createBookData() throws IOException {
        Book book = new Book();
        BookDescription bookDescription = new BookDescription();
        Document document = Jsoup.connect(urlSource).get();
        Element productInfo = document.selectFirst("#product-info");
        Element productImage = document.selectFirst("#product-image");
        if (productInfo != null) {
            String author = productInfo.select(".authors").text().replace("Автор: ", "");
            String title = productInfo.attr("data-name");
            String publisher = productInfo.attr("data-pubhouse");
            String series = productInfo.attr("data-series");
            String bookId = productInfo.attr("data-product-id");
            String isbnString = productInfo.select(".isbn").text().replace("ISBN: ", "");
            assert productImage != null;
            String image = Objects.requireNonNull(productImage.selectFirst("img")).attr("data-src");
            bookDescription.setAuthor(author);
            bookDescription.setTitle(title);
            bookDescription.setSeries(series);
            bookDescription.setPublisher(publisher);
            bookDescription.setBookId(bookId);
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
            bookDescription.setIsbns(isbns);
            bookDescription.setImages(images);
            book.setBookDescription(bookDescription);
        } else {
            System.out.println("Product information not found.");
        }
        return book;
    }

    public BookForGroups createBookForGroupsData() throws IOException {
        BookForGroups book = new BookForGroups();
        BookDescriptionForGroups bookDescription = new BookDescriptionForGroups();
        Document document = Jsoup.connect(urlSource).get();
        Element productLeftColumn = document.selectFirst("#product-left-column");
        assert productLeftColumn != null;
        Element productInfo = productLeftColumn.selectFirst("#product-info");
        if (productInfo != null) {
            String bookId = productInfo.attr("data-product-id");
            String isbnString = productInfo.select(".isbn").text().replace("ISBN: ", "");
            bookDescription.setBookId(bookId);
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
            bookDescription.setIsbns(isbns);
            book.setBookDescriptionForGroups(bookDescription);
        } else {
            System.out.println("Product information not found.");
        }
        return book;
    }

    public List<String> getBooksForNthElementsOfSection(List<String> hrefs, int element) {
        List<String> sectionHrefs = null;
        for (int i = 0; i < element; i++) {
            if (hrefs.size() == 0) {
                return null;
            }
            driver.getDriver().get(hrefs.get(i));
            driver.getShortWait10().until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("html")));
            String html = driver.getDriver().getPageSource();
            Document document = Jsoup.parse(html);
            Elements sectionSectionElements = document.select("a.cover");
            sectionHrefs = extractHrefsOfSection(sectionSectionElements);
        }
        assert sectionHrefs != null;
        return new ArrayList<>(sectionHrefs);
    }

    public ArrayList<String> getSectionHrefs(GroupTypes section, String url) throws IOException {
        driver.getDriver().get(url);
        Document document = Jsoup.connect(url).get();
        Elements sectionHrefs = null;
        try {
            sectionHrefs = document.select("." + section.name().toLowerCase()).select("a");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert sectionHrefs != null;
        return new ArrayList<>(extractHrefsOfSection(sectionHrefs));
    }

    private ArrayList<String> extractHrefsOfSection(Elements section) {
        ArrayList<String> arr = new ArrayList<>();
        for (Element el : section) {
            String href = el.attr("href");
            arr.add(this.webPageUrl + href);
        }
        return new ArrayList<>(arr);
    }
}
