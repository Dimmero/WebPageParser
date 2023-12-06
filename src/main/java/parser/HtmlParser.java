package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.entities.*;
import parser.pages.BaseAbstractPage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class HtmlParser extends BaseAbstractPage {
    public <T extends BookInterface<R>, R extends BookDescriptionInterface> T createBookData(Class<T> bookType, Class<R> bookDescriptionType, String urlSource, SeleniumDriver driver) {
        try {
            driver.getDriver().get(urlSource);
            T book = bookType.getDeclaredConstructor().newInstance();
            Document document = Jsoup.connect(urlSource).get();
            R bookDescription = setDescriptionForBook(document, bookDescriptionType);
            book.initializeBook(bookDescription);
            return book;
        } catch (IOException | NoSuchMethodException e) {
            e.getStackTrace();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private <T extends BookDescriptionInterface> T setDescriptionForBook(Document document, Class<T> descriptionType) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, Object> bookData = new HashMap<>();
        T bookDescription = descriptionType.getDeclaredConstructor().newInstance();
        Element productInfo = document.selectFirst("#product-info");
        assert productInfo != null;
        String bookId = productInfo.attr("data-product-id");
        String isbnString = productInfo.select(".isbn").text().replace("ISBN: ", "");
        ArrayList<String> images = new ArrayList<>();
        ArrayList<String> isbns = getArrayOfIsbns(isbnString);
        if (!isbns.contains(Main.mainIsbn) && bookDescription instanceof BookDescription) {
            isbns.add(Main.mainIsbn);
        }
        ArrayList<String> authors = new ArrayList<>();
        try {
            String authorsString = Objects.requireNonNull(productInfo.selectFirst(".authors")).text().replace("Автор:", "");
            String[] arrayOfAuthors = authorsString.split(",");
            for (String aut : arrayOfAuthors) {
                authors.add(aut.trim());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        String title = productInfo.attr("data-name");
        String annotation = document.select("#product-about").select("p").text();
        String publisher = productInfo.attr("data-pubhouse");
        String series = productInfo.attr("data-series");
        Element productImage = document.selectFirst("#product-image");
        assert productImage != null;
        Elements imagesElements = productImage.select("img");
        for (Element elImg : imagesElements) {
            if (elImg.attr("data-src").isBlank()) {
                images.add(elImg.attr("src"));
            } else {
                images.add(elImg.attr("data-src"));
            }
        }
        bookData.put("bookId", bookId);
        bookData.put("authors", authors);
        bookData.put("title", title);
        bookData.put("annotation", annotation);
        bookData.put("publisher", publisher);
        bookData.put("series", series);
        bookData.put("isbns", isbns);
        bookData.put("images", images);
        bookDescription.initializeDescriptionForBook(bookData);
        return bookDescription;
    }


    public void addRelatedBooks2(GroupTypes group, List<String> relatedBooksUrls, int limit, SeleniumDriver driver) {
        ArrayList<BookForGroups<BookDescriptionForGroups>> books = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            if (relatedBooksUrls == null) {
                break;
            }
            if (relatedBooksUrls.size() < limit) {
                limit = relatedBooksUrls.size();
            }
            String sourceUrl = relatedBooksUrls.get(i);
            BookForGroups<BookDescriptionForGroups> book = Main.htmlParser.createBookData(BookForGroups.class, BookDescriptionForGroups.class, sourceUrl, driver);
            books.add(book);
        }
        setRelatedBooksForBook(String.valueOf(group), books);
    }

//    public void addRelatedBooks(GroupTypes group, List<String> relatedBooksUrls, int limit) {
//        ArrayList<BookForGroups<BookDescriptionForGroups>> books = new ArrayList<>();
//        for (int i = 0; i < limit; i++) {
//            if (relatedBooksUrls == null) {
//                break;
//            }
//            if (relatedBooksUrls.size() < limit) {
//                limit = relatedBooksUrls.size();
//            }
//            String sourceUrl = relatedBooksUrls.get(i);
//            BookForGroups<BookDescriptionForGroups> book = Main.htmlParser.createBookData(BookForGroups.class, BookDescriptionForGroups.class, sourceUrl);
//            books.add(book);
//        }
//        setRelatedBooksForBook(String.valueOf(group), books);
//    }

    private void setRelatedBooksForBook(String group, ArrayList<BookForGroups<BookDescriptionForGroups>> relatedBooks) {
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

    public List<String> getBooksForNthElementsOfSection(String sectionUrl, SeleniumDriver driver) {
        List<String> sectionUrls;
        if (sectionUrl.isBlank()) {
            return null;
        }
        driver.getDriver().get(sectionUrl);
        String html = driver.getDriver().getPageSource();
//        driver.get(sectionUrl);
//        String html = driver.getPageSource();
        Document document = Jsoup.parse(html);
        String elementOfSection = "a.cover";
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

    private ArrayList<String> getArrayOfIsbns(String isbnString) {
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
        return new ArrayList<>(Arrays.asList(arrayOfIsbns));
    }
}
