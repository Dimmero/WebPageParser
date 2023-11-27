package parser;

import parser.entities.Book;
import parser.entities.BookDescription;
import parser.entities.BookDescriptionInterface;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import static parser.pages.BaseAbstractPage.driver;

public class Main {
    public static Book<BookDescription> book = new Book<>();
    public static String FILE_NAME_PATH = "/books.json";
    public static String LOGS_PATH = "/logs";
    public static String webPageUrl = System.getenv("PARSE_SERVICE");
    public static HtmlParser htmlParser = new HtmlParser();
    public static String mainIsbn;

    public static void main(String[] args) throws IOException, URISyntaxException {
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
                book = htmlParser.createBookData(Book.class, BookDescription.class, mainBookUrl);
                String seriesUrl = htmlParser.getSectionUrl(GroupTypes.SERIES, mainBookUrl);
                String authorsUrl = htmlParser.getSectionUrl(GroupTypes.AUTHORS, mainBookUrl);
                String genresUrl = htmlParser.getSectionUrl(GroupTypes.GENRE, mainBookUrl);
                List<String> seriesBooks = htmlParser.getBooksForNthElementsOfSection(seriesUrl);
                List<String> authorsBooks = htmlParser.getBooksForNthElementsOfSection(authorsUrl);
                List<String> genresBooks = htmlParser.getBooksForNthElementsOfSection(genresUrl);
                htmlParser.addRelatedBooks(GroupTypes.SERIES, seriesBooks, limitOfRelatedBooks);
                htmlParser.addRelatedBooks(GroupTypes.AUTHORS, authorsBooks, limitOfRelatedBooks);
                htmlParser.addRelatedBooks(GroupTypes.GENRE, genresBooks, limitOfRelatedBooks);
                jsonWriter.writeBookToFile(book);
                book = null;
            }
            driver.closeDriver();
        } catch (URISyntaxException e) {
            e.printStackTrace();
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