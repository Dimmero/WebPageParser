package parser;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.entities.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class HtmlParser {

    public ArrayList<String> getMainBooksUrl(String isbn) throws IOException {
        String urlSource = "https://www.labirint.ru/search/" + isbn + "/?stype=0";
        Document document = Jsoup.connect(urlSource).get();
        Elements urls = document.select(".product-card__img");
        ArrayList<String> mainBooksUrls = new ArrayList<>();
        urls.forEach(url -> mainBooksUrls.add(Main.webPageUrl + url.attr("href")));
        return mainBooksUrls;
    }

    public <T extends BookInterface<R>, R extends BookDescriptionInterface> T createBookData(Class<T> bookType, Class<R> bookDescriptionType, String urlSource) {
        try {
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
        String annotation = (document.select("#smallannotation").size() == 0)
                ? Objects.requireNonNull(document.select("#product-about").select("p").first()).text()
                : Objects.requireNonNull(document.select("#product-about").select("p").last()).text();
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

    public void addNthRelatedBooks(GroupTypes group, String url, int limit, boolean useParallel) throws IOException {
        List<String> relatedBooks = getBooksForSection(getSectionUrl(group, url));
        if (relatedBooks == null || relatedBooks.isEmpty()) {
            return;
        }
        int actualLimit = Math.min(limit, relatedBooks.size());
        List<BookForGroups<BookDescriptionForGroups>> books = Collections.synchronizedList(new ArrayList<>());
        if (useParallel) {
            List<Callable<Void>> tasks = relatedBooks.subList(0, actualLimit).stream()
                    .map(bookUrl -> (Callable<Void>) () -> {
                        HtmlParser parser = new HtmlParser();
                        Thread.sleep(150);
                        books.add(parser.createBookData(BookForGroups.class, BookDescriptionForGroups.class, bookUrl));
                        return null;
                    })
                    .collect(Collectors.toList());
            ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
            try {
                List<Future<Void>> futures = executorService.invokeAll(tasks);
                for (Future<Void> future : futures) {
                    future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                executorService.shutdown();
            }
        } else {
            relatedBooks.subList(0, actualLimit)
                    .forEach(urlBook -> {
                        BookForGroups<BookDescriptionForGroups> book = this.createBookData(BookForGroups.class, BookDescriptionForGroups.class, urlBook);
                        books.add(book);
                    });
        }
        setRelatedBooksForBook(group, books);
    }

    private void setRelatedBooksForBook(GroupTypes group, List<BookForGroups<BookDescriptionForGroups>> relatedBooks) {
        switch (group) {
            case SERIES: {
                Main.book.setBooksOfSeries(relatedBooks);
                break;
            }
            case AUTHORS: {
                Main.book.setBooksOfAuthor(relatedBooks);
                break;
            }
            case GENRE: {
                Main.book.setBooksOfGenre(relatedBooks);
                break;
            }
        }
    }

    private List<String> getBooksForSection(String sectionUrl) throws IOException {
        List<String> sectionUrls;
        if (sectionUrl.isBlank()) {
            return null;
        }
        Document document = Jsoup.connect(sectionUrl).get();
        String elementOfSection = "a.cover";
        Elements sectionElements = document.select(elementOfSection);
        sectionUrls = extractUrlOfSection(sectionElements);
        return sectionUrls;
    }

    public String getSectionUrl(GroupTypes section, String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        String urlOfSection = "";
        String sectionName = section.name().toLowerCase();
        Element sectionUrl;
        try {
            if (section == GroupTypes.GENRE) {
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
