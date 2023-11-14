package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Book;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonWriter {
    private String path = "books.json";
    private File file;
    private List<Book> booksList;
    private ObjectMapper objectMapper;

    public JsonWriter(String pathJar) {
        this.file = new File(pathJar + this.path);
        this.booksList = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
    }

    public List<Book> getBooksList() {
        return booksList;
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
