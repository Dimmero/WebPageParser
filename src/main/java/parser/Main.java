package parser;

import parser.entities.Book;
import parser.entities.BookDescription;
import parser.entities.ParserType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Main {
    public static Book<BookDescription> book = new Book<>();
    public static String FILE_NAME_PATH = "/books.json";
    public static String LOGS_PATH = "/logs";
    public static String webPageUrl;
    public static String webPageUrlGorod = System.getenv("PARSE_SERVICE_GOROD");
    public static String webPageUrlIKniga = System.getenv("PARSE_SERVICE_IKNIGA");
    public static String mainIsbn;
    private static final Object bookLock = new Object();
    private static boolean webDriver;
    private static boolean oneThread;
    private static boolean inParallel;
    public static ParserType parserType;
    private static int depth;
    private static String jarPath;
    private static final HtmlParser htmlParser = new HtmlParser();
    private static JsonWriter jsonWriter = null;

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println(LocalDateTime.now());
        initialize(args);
        try {
            processBooks();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println(LocalDateTime.now());
    }

    private static void initialize(String[] args) throws URISyntaxException {
        mainIsbn = args[0];
        depth = Integer.parseInt(args[1]);
        oneThread = Boolean.parseBoolean(args[2]);
        inParallel = Boolean.parseBoolean(args[3]);
        webDriver = Boolean.parseBoolean(args[4]);
        parserType = ParserType.valueOf(args[5]);
        if (parserType.equals(ParserType.LABIRINT)) {
            webPageUrl = System.getenv("PARSE_SERVICE");
        } else if (parserType.equals(ParserType.GOROD)) {
            webPageUrl = System.getenv("PARSE_SERVICE_GOROD");
        } else if (parserType.equals(ParserType.FKNIGA)) {
            webPageUrl = System.getenv("PARSE_SERVICE_FKNIGA");
        }
//        webPageUrl = parserType.equals(ParserType.LABIRINT) ? System.getenv("PARSE_SERVICE")
//                : System.getenv("PARSE_SERVICE_GOROD");
        setJarPath();
        jsonWriter = new JsonWriter(jarPath + FILE_NAME_PATH);
    }

    private static void setJarPath() throws URISyntaxException {
        File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        jarPath = jarFile.getParent();
    }

    private static void processBooks() throws IOException, URISyntaxException {
        List<String> mainBookUrls = getMainBookUrls();
        if (mainBookUrls != null) {
            for (String mainBookUrl : mainBookUrls) {
                processSingleBook(mainBookUrl);
            }
        }
    }

    private static List<String> getMainBookUrls() throws IOException, URISyntaxException {
        if (webDriver) {
            SearchBookByIsbnFeature search = new SearchBookByIsbnFeature();
            return search.provideIsbnAndGoToBookPage(mainIsbn);
        } else {
            return htmlParser.getMainBooksUrl(mainIsbn);
        }
    }

    private static void processSingleBook(String mainBookUrl) throws IOException {
        book = htmlParser.createBookData(Book.class, BookDescription.class, mainBookUrl);
        if (oneThread) {
            processBooksWithSingleThread(mainBookUrl);
        } else {
            processBooksWithThreads(mainBookUrl);
        }
        writeBookToFile();
        book = null;
    }

    private static void processBooksWithSingleThread(String mainBookUrl) throws IOException {
        for (GroupTypes groupType : GroupTypes.values()) {
            htmlParser.addNthRelatedBooks(groupType, mainBookUrl, depth, inParallel);
        }
    }

    private static void processBooksWithThreads(String mainBookUrl) {
        List<Callable<Void>> tasks = Arrays.stream(GroupTypes.values())
                .map(groupType -> (Callable<Void>) () -> {
                    HtmlParser parser = new HtmlParser();
                    parser.addNthRelatedBooks(groupType, mainBookUrl, depth, inParallel);
                    return null;
                })
                .collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }
    }

    private static void writeBookToFile() {
        synchronized (bookLock) {
            jsonWriter.writeBookToFile(book);
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