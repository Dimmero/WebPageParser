package parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import parser.entities.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
public class HtmlParser {
    private final String LAB_SEARCH_QUERY = "https://www.labirint.ru/search/";
    private final String GOR_SEARCH_QUERY = "https://www.chitai-gorod.ru/search?phrase=";
    private String urlSource;
    private String cssQuery;
    private String labirintImageUrl;

    public List<String> getMainBooksUrl(String isbn) throws IOException {
        if (Main.parserType.equals(ParserType.LABIRINT)) {
            urlSource = "https://www.labirint.ru/search/" + isbn + "/?stype=0";
            cssQuery = "//*[@class='product-card__img']";
//            cssQuery = ".product-card__img";
        } else if (Main.parserType.equals(ParserType.GOROD)) {
            urlSource = "https://www.chitai-gorod.ru/search?phrase=" + isbn;
            cssQuery = "(//article//*[@class='product-card__title'])[1]";
//            cssQuery = ".product-card__picture";
        } else if (Main.parserType.equals(ParserType.FKNIGA)) {
            urlSource = "https://fkniga.ru/search/?q=" + isbn;
            cssQuery = "//div[contains(@class, 'card__body')]//a[contains(@class, 'card__title')]";
            List<String> urlsWithHash = getMainBooksFromSource();
            if (urlsWithHash == null) {
                return null;
            }
            return formatUrlsWithHash(urlsWithHash);
        }
        return getMainBooksFromSource();
    }

    public List<String> formatUrlsWithHash(List<String> urlsWithHash) {
        return urlsWithHash.stream()
                .map(url -> url.split("\\?")[0]) // Remove query parameters
                .collect(Collectors.toList());   // Collect results into a new list
    }

//    private ArrayList<String> getMainBooksFromSource() throws IOException {
//        ArrayList<String> mainBooksUrls = new ArrayList<>();
//        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "false");
//        System.setProperty("jdk.http.auth.proxying.disabledSchemes", "false");
//        System.setProperty("https.proxyHost", "43.152.113.55");
//        System.setProperty("https.proxyPort", "2334");
//        Authenticator.setDefault(new Authenticator() {
//            @Override
//            public PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication("akudzm", "Metyl123".toCharArray());
//            }
//        });
//        Connection connection = Jsoup.connect(urlSource);
//        connection.proxy("43.152.113.55", 2334);
//        connection.method(Connection.Method.GET);
//        connection.ignoreContentType(true);
//        try {
//            Connection.Response response = connection.execute();
//            Document document = response.parse();
//            Elements urls = document.select(cssQuery);
//            urls.forEach(url -> mainBooksUrls.add(Main.webPageUrl + url.attr("href")));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return mainBooksUrls;
//    }

    private List<String> getMainBooksFromSource() throws IOException {
        ArrayList<String> mainBooksUrls = new ArrayList<>();
        Document document = Jsoup.connect(urlSource).get();
        Element button = document.selectFirst(".change--search");
        if (button != null) {
            return null;
        }
        Elements urls = document.selectXpath(cssQuery);
        urls.forEach(url -> mainBooksUrls.add(Main.webPageUrl + url.attr("href")));
        return mainBooksUrls;
    }

    public <T extends BookInterface<R>, R extends BookDescriptionInterface> T createBookData(Class<T> bookType, Class<R> bookDescriptionType, String urlSource) {
        try {
            T book = bookType.getDeclaredConstructor().newInstance();
            Document document = Jsoup.connect(urlSource).get();
            R bookDescription = null;
            if (Main.parserType.equals(ParserType.LABIRINT)) {
                bookDescription = setDescriptionForLabirintBook(document, bookDescriptionType);
            } else if (Main.parserType.equals(ParserType.GOROD)) {
                bookDescription = setDescriptionForGorodBook(document, bookDescriptionType);
            } else if (Main.parserType.equals(ParserType.FKNIGA)) {
                bookDescription = setDescriptionForFknigaBook(document, bookDescriptionType);
            }
            book.initializeBook(bookDescription);
            return book;
        } catch (IOException | NoSuchMethodException e) {
            e.getStackTrace();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private <T extends BookDescriptionInterface> T setDescriptionForLabirintBook(Document document, Class<T> descriptionType) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, JsonProcessingException {
        Map<String, Object> bookData = new HashMap<>();
        T bookDescription = descriptionType.getDeclaredConstructor().newInstance();
        String bookId = document.selectXpath("//meta[@itemprop='sku']").attr("content");
        String isbnString = document.selectXpath("//meta[@itemprop='isbn']").attr("content");
        ArrayList<String> images = new ArrayList<>();
        ArrayList<String> isbns = getArrayOfIsbns(isbnString);
        List<String> authors = new ArrayList<>();
        if (!isbns.contains(Main.mainIsbn) && bookDescription instanceof BookDescription) {
            isbns.add(Main.mainIsbn);
        }
        try {
            Elements authorEls = document.selectXpath("//div[@id='сharacteristics']//div[contains(text(), 'Автор')]/..//a");
            Elements editorEls = document.selectXpath("//div[@id='сharacteristics']//div[contains(text(), 'Редактор')]/..//a");

            if (!authorEls.isEmpty()) {
                authors = authorEls.stream().map(Element::text).collect(Collectors.toList());
            } else if (!editorEls.isEmpty()) {
                authors = editorEls.stream().map(Element::text).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String title = document.selectXpath("//h1[@itemprop='name']").text();
        String annotationPartial = document.selectXpath("//div[@id='annotation']//div[contains(@class, 'content')]").text();
        Elements scriptWithAnnotation = document.selectXpath("//footer//following::script[@id='__NUXT_DATA__']");
        Element element = scriptWithAnnotation.get(0);
        Pattern pattern = Pattern.compile("\"Ру\",\"(.+?)\",\\[");
        Matcher matcher = pattern.matcher(element.html());

        String annotationHtml = null;
        if (matcher.find()) {
            annotationHtml = matcher.group(1);
        }
        assert annotationHtml != null;
        ObjectMapper mapper = new ObjectMapper();
        String unescapedJson = mapper.readValue("\"" + annotationHtml + "\"", String.class);

        // Step 2: Decode HTML entities and strip tags
        String htmlDecoded = Parser.unescapeEntities(unescapedJson, true);
        String annotation = Jsoup.parse(htmlDecoded).text();

        String publisher = document.selectXpath("//div[@id='сharacteristics']//div[contains(text(), 'Издательство')]/..//a").text();
        String series = document.selectXpath("//div[@id='сharacteristics']//div[contains(text(), 'Серия')]/..//a").text();
        String image = document.selectXpath("//meta[@itemprop='image']").attr("content").replace("//", "");
        images.add(image);
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

    private String getGoodFirstImage() {
        try {
            Document document = Jsoup.connect(urlSource).get();
            Element productImage = document.selectFirst(cssQuery);
            assert productImage != null;
            Elements imagesElements = productImage.select("img");
            for (Element elImg : imagesElements) {
                if (elImg.attr("data-src").isBlank()) {
                    return elImg.attr("src");
                } else {
                    return elImg.attr("data-src");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private <T extends BookDescriptionInterface> T setDescriptionForGorodBook(Document document, Class<T> descriptionType) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, Object> bookData = new HashMap<>();
        T bookDescription = descriptionType.getDeclaredConstructor().newInstance();
        ArrayList<String> images = new ArrayList<>();
        ArrayList<String> isbns = new ArrayList<>();
        String bookId = document.selectXpath("(//span[contains(text(), 'ID товара')]/following-sibling::span)[1]").text();
        Elements isbnsEls = document.selectXpath("//span[@itemprop='isbn']");
        isbnsEls.forEach(isbn -> isbns.add(isbn.text().replaceAll("-", "")));
        if (!isbns.contains(Main.mainIsbn.replaceAll("-", "")) && bookDescription instanceof BookDescription) {
            isbns.add(Main.mainIsbn);
        }
        if (bookDescription instanceof BookDescription) {
            Elements imagesEls = document.selectXpath("//*[@class='product-media__thumbnails']//li//img");
            for (Element im : imagesEls) {
                String src = im.attr("src");
                String cleaned = src.replaceAll("\\.jpg.*$", ".jpg");
                images.add(cleaned);
            }
//            String mainImage = document.selectXpath("//meta[@property='og:image']").attr("content");
//            images.add(mainImage);
//            images.addAll(getGorodBookOtherImages(mainImage));
        }
        ArrayList<String> authors = new ArrayList<>();
        Elements authorsElms = document.selectXpath("//a[@class='product-info-authors__author']");
        authorsElms.forEach(aut -> authors.add(aut.text().replace(",", "")));
        String title = document.selectXpath("//h1[@class='detail-product__header-title']").text();
        String annotation = document.selectXpath("//article[@class='detail-description__text']").text();
        String publisher = document.selectXpath("(//div[@class='product-detail-features__item']//*[@itemprop='publisher'])[1]").text();
        String series = document.selectXpath("(//div[@class='product-detail-features__item']//a)[2]").text();
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

    private <T extends BookDescriptionInterface> T setDescriptionForFknigaBook(Document document, Class<T> descriptionType) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        String latestYear = document.selectXpath("//div[@class='goodCard__tabs']//a[1]").attr("href");
        if (!latestYear.isEmpty()) {
            latestYear = Main.webPageUrl + formatUrlsWithHash(List.of(latestYear)).get(0);
            document = Jsoup.connect(latestYear).get();
        }


        Map<String, Object> bookData = new HashMap<>();
        T bookDescription = descriptionType.getDeclaredConstructor().newInstance();
        String bookId = document.selectXpath("(//div[contains(@class, 'goodCard__article') and contains(text(), 'Артикул')])[1]").text().replace("Артикул", "").trim();
        String title = document.selectXpath("(//h1)[1]").text().split("\\|")[0].trim();

        String publisher = document.selectXpath("//span[contains(text(), 'Издательство:')]/..")
                .text().replace("Издательство:", "").trim();
        String brand = document.selectXpath("//span[contains(text(), 'Брэнд:')]/..")
                .text().replace("Брэнд:", "").trim();
        String publisherFinalValue;
        if (!publisher.isEmpty()) {
            publisherFinalValue = publisher;
        } else if (!brand.isEmpty()) {
            publisherFinalValue = brand;
        } else {
            publisherFinalValue = "Неизвестно";
        }
        ArrayList<String> isbns = new ArrayList<>();
        String isbn = document.selectXpath("//span[contains(text(), 'ISBN:')]/..").text().replace("ISBN:", "").replace("-", "").trim();
        isbns.add(isbn);
        ArrayList<String> authors = new ArrayList<>();
        Elements authorsEle = document.selectXpath("//span[contains(text(), 'Автор:')]/..");
        if (!authorsEle.isEmpty()) {
            authorsEle.forEach(aut -> authors.add(aut.text().replace("Автор:", "").trim()));
        }
        if (authors.isEmpty()) {
            authors.add("Автор не указан");
        }

        ArrayList<String> images = new ArrayList<>();
        String image = Main.webPageUrl + document.selectXpath("(//img[@class='goodCardImg'])[1]").attr("src");
        images.add(image);
        String annotation = document.selectXpath("//div[@class='container-sm']").text().replace("Описание товара", "").trim();

        String publisherSeries = document.selectXpath("//h5[contains(text(), 'Издательская серия')]/..//a")
                .text().replace("Издательская серия:", "").trim();
        String publishingProgram = document.selectXpath("//h5[contains(text(), 'Образовательная программа')]/..//a")
                .text().replace("Брэнд:", "").trim();
        String publisherSeriesFinalValue;
        if (!publisherSeries.isEmpty()) {
            publisherSeriesFinalValue = publisherSeries;
        } else if (!publishingProgram.isEmpty()) {
            publisherSeriesFinalValue = publishingProgram;
        } else {
            publisherSeriesFinalValue = "Неизвестно";
        }

        bookData.put("bookId", bookId);
        bookData.put("title", title);
        bookData.put("isbns", isbns);
        bookData.put("authors", authors);
        bookData.put("annotation", annotation);
        bookData.put("publisher", publisherFinalValue);
        bookData.put("images", images);
        bookData.put("series", publisherSeriesFinalValue);
        bookDescription.initializeDescriptionForBook(bookData);
        return bookDescription;
    }

    private List<String> getGorodBookOtherImages(String url) {
        List<String> imageList = new ArrayList<>();
        try {
//            String query = "?width=310&height=500&fit=bounds";
            String imageUrl;
            for (int i = 1; i < 100; i++) {
                imageUrl = url.replaceAll("(.*?)(-\\d+)?(\\.jpg)", "$1_" + i + "$2$3");
                URL urlRequest = new URI(imageUrl).toURL();
                HttpURLConnection connection = (HttpURLConnection) urlRequest.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.getInputStream();
//                Thread.sleep(10000);
                imageList.add(imageUrl);
            }
            return imageList;
        } catch (IOException | URISyntaxException /*| InterruptedException */e) {
            e.getStackTrace();
            return imageList;
        }
    }

    private String extractBaseUrl(String url) {
        Pattern pattern = Pattern.compile("^(.*?)(\\?.*)?$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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
        String elementOfSection = Main.parserType.equals(ParserType.LABIRINT) ? "a.cover" : ".product-card__picture";
        Elements sectionElements = document.select(elementOfSection);
        sectionUrls = extractUrlOfSection(sectionElements);
        return sectionUrls;
    }

    public String getSectionUrl(GroupTypes section, String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        boolean isLabirint = Main.parserType.equals(ParserType.LABIRINT);

        try {
            Element sectionUrl = getSectionElement(document, section, isLabirint);

            if (sectionUrl != null) {
                String urlResult = sectionUrl.attr("href");
                if (!urlResult.contains("https")) {
                    urlResult = Main.webPageUrl + urlResult;
                }
                return urlResult; // Returning the correct URL
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private Element getSectionElement(Document document, GroupTypes section, boolean isLabirint) {
        if (section == GroupTypes.GENRE) {
            return isLabirint
                    ? document.selectXpath("//span[@itemprop='itemListElement']//a").last()
                    : document.selectXpath("//a[@class='product-breadcrumbs__link']").last();
        }
        if (section == GroupTypes.AUTHORS) {
            if (isLabirint) {
                Element author = document.selectXpath("//div[@id='сharacteristics']//div[contains(text(), 'Автор')]/..//a").first();
                Element editor = document.selectXpath("//div[@id='сharacteristics']//div[contains(text(), 'Редактор')]/..//a").first();
                if (author != null) {
                    // If "Автор" exists, use it
                    return author;
                } else if (editor != null) {
                    // If "Автор" is missing, fallback to "Редактор"
                    return editor;
                }
            } else {
                document.selectXpath("//a[@class='product-info-authors__author']").first();
            }
        }
        if (section == GroupTypes.SERIES) {
            return isLabirint
                    ? document.selectXpath("//h2[contains(text(), 'Книги из серии ')]//a").first()
                    : document.selectXpath("//a[contains(@href, 'series')]").first();
        }
        return null;
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
            arrayOfIsbns[i] = arrayOfIsbns[i].replace("-", "").trim();
        }
        return new ArrayList<>(Arrays.asList(arrayOfIsbns));
    }

    private ArrayList<String> getArrayOfAuthors(String authorString) {
        String[] arrayOfAuthors = authorString.split(",");
        for (int i = 0; i < arrayOfAuthors.length; i++) {
            if (arrayOfAuthors[i].contains("все") || arrayOfAuthors[i].contains("скрыть")) {
                arrayOfAuthors[i] = arrayOfAuthors[i].replace("все", "");
                arrayOfAuthors[i] = arrayOfAuthors[i].replace("скрыть", "");
            }
        }
        return new ArrayList<>(Arrays.asList(arrayOfAuthors));
    }
}

//String title = document.selectXpath("//h1[@class='detail-product__header-title']").text();
//String annotation = document.selectXpath("//article[@class='detail-description__text']").text();
//String publisher = document.selectXpath("(//div[@class='product-detail-features__item']//*[@itemprop='publisher'])[1]").text();
//String series = document.selectXpath("(//div[@class='product-detail-features__item']//a)[2]").text();