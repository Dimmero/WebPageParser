package parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import parser.entities.Book;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonWriter {
    private final File file;
    private List<Book> booksList;
    private final ObjectMapper objectMapper;

    public JsonWriter(String pathJar) {
        this.file = new File(pathJar + "books.json");
        this.booksList = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
    }

    public List<Book> getBooksList() {
        return booksList;
    }

    private void fetchBooksFromFile() {
        if (file.exists()) {
            try {
                Book[] booksArray = objectMapper.readValue(file, Book[].class);
                booksList = Arrays.asList(booksArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeBookToFile(Book book) {
        fetchBooksFromFile();
        booksList.add(book);
        try {
            objectMapper.writeValue(file, booksList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
