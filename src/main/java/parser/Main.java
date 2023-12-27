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
            HtmlParser htmlParser = new HtmlParser();
            List<String> mainBookUrls = htmlParser.getMainBooksUrl(mainIsbn);
            if (mainBookUrls == null) {
                return;
            }
            for (String mainBookUrl : mainBookUrls) {
                book = htmlParser.createBookData(Book.class, BookDescription.class, mainBookUrl);
                if (oneThread) {
                    htmlParser.addNthRelatedBooks(GroupTypes.SERIES, mainBookUrl, limitOfRelatedBooks);
                    htmlParser.addNthRelatedBooks(GroupTypes.AUTHORS, mainBookUrl, limitOfRelatedBooks);
                    htmlParser.addNthRelatedBooks(GroupTypes.GENRE, mainBookUrl, limitOfRelatedBooks);
                } else {
                    List<Callable<Void>> tasks = Arrays.asList(
                            () -> {
                                HtmlParser parser = new HtmlParser();
                                parser.addNthRelatedBooks(GroupTypes.SERIES, mainBookUrl, limitOfRelatedBooks);
                                return null;
                            },
                            () -> {
                                HtmlParser parser = new HtmlParser();
                                parser.addNthRelatedBooks(GroupTypes.AUTHORS, mainBookUrl, limitOfRelatedBooks);
                                return null;
                            },
                            () -> {
                                HtmlParser parser = new HtmlParser();
                                parser.addNthRelatedBooks(GroupTypes.GENRE, mainBookUrl, limitOfRelatedBooks);
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