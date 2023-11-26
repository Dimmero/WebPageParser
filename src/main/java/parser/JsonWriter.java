package parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import parser.entities.Book;
import parser.entities.BookDescriptionInterface;
import parser.entities.BookInterface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonWriter {
    private final File file;
    private List<Book<BookDescriptionInterface>> booksList;
    private final ObjectMapper objectMapper;

    public JsonWriter(String pathJar) {
        this.file = new File(pathJar);
        this.booksList = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
    }

    private void fetchBooksFromFile() {
        if (file.exists()) {
            try {
                booksList = objectMapper.readValue(file, new TypeReference<>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeBookToFile(Book<BookDescriptionInterface> book) {
        fetchBooksFromFile();
        booksList.add(book);
        try {
            objectMapper.writeValue(file, booksList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
