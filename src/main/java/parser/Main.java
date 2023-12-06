package parser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import parser.entities.Book;
import parser.entities.BookDescription;
import parser.entities.BookDescriptionInterface;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static parser.pages.BaseAbstractPage.driver;

public class Main {
    public static Book<BookDescription> book = new Book<>();
    public static String FILE_NAME_PATH = "/books.json";
    public static String LOGS_PATH = "/logs";
    public static String webPageUrl = System.getenv("PARSE_SERVICE");
    public static HtmlParser htmlParser = new HtmlParser();
    public static String mainIsbn;
    private static final Object bookLock = new Object();

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println(LocalDateTime.now());
        mainIsbn = args[0];
        int limitOfRelatedBooks = Integer.parseInt(args[1]);
        String jarPath;
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            jarPath = jarFile.getParent();
            JsonWriter jsonWriter = new JsonWriter(jarPath + FILE_NAME_PATH);
//            deleteLogFiles(jarPath + LOGS_PATH);
            SearchBookByIsbnFeature searchBookByIsbnFeature = new SearchBookByIsbnFeature();
            List<String> mainBookUrls = searchBookByIsbnFeature.provideIsbnAndGoToBookPage(mainIsbn, webPageUrl);
            for (String mainBookUrl : mainBookUrls) {
                book = htmlParser.createBookData(Book.class, BookDescription.class, mainBookUrl, driver);
                String seriesUrl = htmlParser.getSectionUrl(GroupTypes.SERIES, mainBookUrl);
                String authorsUrl = htmlParser.getSectionUrl(GroupTypes.AUTHORS, mainBookUrl);
                String genresUrl = htmlParser.getSectionUrl(GroupTypes.GENRE, mainBookUrl);
                driver.closeDriver();
//                List<String> seriesBooks = htmlParser.getBooksForNthElementsOfSection(seriesUrl);
//                List<String> authorsBooks = htmlParser.getBooksForNthElementsOfSection(authorsUrl);
//                List<String> genresBooks = htmlParser.getBooksForNthElementsOfSection(genresUrl);
//                htmlParser.addRelatedBooks(GroupTypes.SERIES, seriesBooks, limitOfRelatedBooks);
//                htmlParser.addRelatedBooks(GroupTypes.AUTHORS, authorsBooks, limitOfRelatedBooks);
//                htmlParser.addRelatedBooks(GroupTypes.GENRE, genresBooks, limitOfRelatedBooks);
                List<Callable<Void>> tasks = Arrays.asList(
                        () -> {
                            SeleniumDriver seriesDriver = new SeleniumDriver();
                            List<String> seriesBooks = htmlParser.getBooksForNthElementsOfSection(seriesUrl, seriesDriver);
                            htmlParser.addRelatedBooks2(GroupTypes.SERIES, seriesBooks, limitOfRelatedBooks, seriesDriver);
                            seriesDriver.closeDriver();
                            return null;
                        },
                        () -> {
                            SeleniumDriver authorsDriver = new SeleniumDriver();
                            List<String> authorsBooks = htmlParser.getBooksForNthElementsOfSection(authorsUrl, authorsDriver);
                            htmlParser.addRelatedBooks2(GroupTypes.AUTHORS, authorsBooks, limitOfRelatedBooks, authorsDriver);
                            authorsDriver.closeDriver();
                            return null;
                        },
                        () -> {
                            SeleniumDriver genreDriver = new SeleniumDriver();
                            List<String> genresBooks = htmlParser.getBooksForNthElementsOfSection(genresUrl, genreDriver);
                            htmlParser.addRelatedBooks2(GroupTypes.GENRE, genresBooks, limitOfRelatedBooks, genreDriver);
                            genreDriver.closeDriver();
                            return null;
                        }
                );
                ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
                executorService.invokeAll(tasks);
                executorService.shutdown();
                synchronized (bookLock) {
                    jsonWriter.writeBookToFile(book);
                }
                book = null;
            }
//            driver.closeDriver();
            System.out.println(LocalDateTime.now().getMinute());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteLogFiles(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            if (file.delete()) {
                                System.out.println("File deleted successfully");
                            } else {
                                System.out.println("Unable to delete the file");
                            }
                        } catch (SecurityException e) {
                            System.err.println("SecurityException: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}