package parser;

import parser.entities.Book;
import parser.entities.BookDescription;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static Book<BookDescription> book = new Book<>();
    public static String FILE_NAME_PATH = "/books.json";
    public static String LOGS_PATH = "/logs";
    public static String webPageUrl = System.getenv("PARSE_SERVICE");
    public static String mainIsbn;
    private static final Object bookLock = new Object();

    public static void main(String[] args) throws IOException {
        System.out.println(LocalDateTime.now());
        mainIsbn = args[0];
        int limitOfRelatedBooks = Integer.parseInt(args[1]);
        boolean oneThread = Boolean.parseBoolean(args[2]);
        String jarPath;
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            jarPath = jarFile.getParent();
            JsonWriter jsonWriter = new JsonWriter(jarPath + FILE_NAME_PATH);
            SearchBookByIsbnFeature searchBookByIsbnFeature = new SearchBookByIsbnFeature();
            List<String> mainBookUrls = searchBookByIsbnFeature.provideIsbnAndGoToBookPage(mainIsbn, webPageUrl);
            HtmlParser htmlParser = new HtmlParser();
            for (String mainBookUrl : mainBookUrls) {
                book = htmlParser.createBookData(Book.class, BookDescription.class, mainBookUrl);
                String seriesUrl = htmlParser.getSectionUrl(GroupTypes.SERIES, mainBookUrl);
                String authorsUrl = htmlParser.getSectionUrl(GroupTypes.AUTHORS, mainBookUrl);
                String genresUrl = htmlParser.getSectionUrl(GroupTypes.GENRE, mainBookUrl);
                if (oneThread) {
                    List<String> seriesBooks = htmlParser.getBooksForNthElementsOfSection(seriesUrl);
                    List<String> authorsBooks = htmlParser.getBooksForNthElementsOfSection(authorsUrl);
                    List<String> genresBooks = htmlParser.getBooksForNthElementsOfSection(genresUrl);
                    htmlParser.addRelatedBooks(GroupTypes.SERIES, seriesBooks, limitOfRelatedBooks);
                    htmlParser.addRelatedBooks(GroupTypes.AUTHORS, authorsBooks, limitOfRelatedBooks);
                    htmlParser.addRelatedBooks(GroupTypes.GENRE, genresBooks, limitOfRelatedBooks, true);
                } else {
                    htmlParser.getDriver().closeDriver();
                    List<Callable<Void>> tasks = Arrays.asList(
                            () -> {
                                HtmlParser parser = new HtmlParser();
                                List<String> seriesBooks = parser.getBooksForNthElementsOfSection(seriesUrl);
                                parser.addRelatedBooks(GroupTypes.SERIES, seriesBooks, limitOfRelatedBooks, true);
                                return null;
                            },
                            () -> {
                                HtmlParser parser = new HtmlParser();
                                List<String> authorsBooks = parser.getBooksForNthElementsOfSection(authorsUrl);
                                parser.addRelatedBooks(GroupTypes.AUTHORS, authorsBooks, limitOfRelatedBooks, true);
                                return null;
                            },
                            () -> {
                                HtmlParser parser = new HtmlParser();
                                List<String> genresBooks = parser.getBooksForNthElementsOfSection(genresUrl);
                                parser.addRelatedBooks(GroupTypes.GENRE, genresBooks, limitOfRelatedBooks, true);
                                return null;
                            }
                    );
                    ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
                    executorService.invokeAll(tasks);
                    executorService.shutdown();
                }
                synchronized (bookLock) {
                    jsonWriter.writeBookToFile(book);
                }
                book = null;
            }
            System.out.println(LocalDateTime.now());
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