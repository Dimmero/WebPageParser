package parser;

import parser.entities.Book;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static Book book = new Book();
    public static String webPageUrl;

    public static void main(String[] args) throws IOException, URISyntaxException {
        String isbn = args[0];
        webPageUrl = System.getenv("PARSE_SERVICE");
        String jarPath = "";
         try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            jarPath = jarFile.getParent();
            JsonWriter jsonWriter = new JsonWriter(jarPath);
            String logDirectoryPath = jarPath + "/logs";
            deleteLogFiles(logDirectoryPath);
            SearchBookByIsbnFeature searchBookByIsbnFeature = new SearchBookByIsbnFeature();
//        JsonWriter jsonWriter = new JsonWriter("");
//        String url = searchBookByIsbnFeature.provideIsbnAndGoToBookPage("9785090648264");
            String url = searchBookByIsbnFeature.provideIsbnAndGoToBookPage(isbn, webPageUrl);
            HtmlParser htmlParser = new HtmlParser(url, webPageUrl);
            book = htmlParser.createBookData();
            ArrayList<String> seriesHrefs = htmlParser.getSectionHrefs(GroupTypes.SERIES, url);
            ArrayList<String> authorsHrefs = htmlParser.getSectionHrefs(GroupTypes.AUTHORS, url);
            ArrayList<String> genresHrefs = htmlParser.getSectionHrefs(GroupTypes.GENRE, url);
            List<String> seriesBooks = htmlParser.getBooksForNthElementsOfSection(seriesHrefs, 1);
            List<String> authorsBooks = htmlParser.getBooksForNthElementsOfSection(authorsHrefs, 1);
            List<String> genresBooks = htmlParser.getBooksForNthElementsOfSection(genresHrefs, 1);
            searchBookByIsbnFeature.addRelatedBooks(GroupTypes.SERIES, seriesBooks, 3);
            searchBookByIsbnFeature.addRelatedBooks(GroupTypes.AUTHORS, authorsBooks, 3);
            searchBookByIsbnFeature.addRelatedBooks(GroupTypes.GENRE, genresBooks, 3);
            SearchBookByIsbnFeature.driver.closeDriver();
            jsonWriter.writeBookToFile(book);
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