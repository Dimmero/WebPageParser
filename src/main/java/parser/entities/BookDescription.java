package parser.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookDescription implements BookDescriptionInterface {
    private ArrayList<String> authors;
    private String title;
    private String annotation;
    private String publisher;
    private String series;
    private String bookId;
    private ArrayList<String> isbns;
    private ArrayList<String> images;

    @Override
    public void initializeDescriptionForBook(Map<String, Object> attributes) {
        setBookId((String) attributes.get("bookId"));
        setTitle((String) attributes.get("title"));
        setIsbns((ArrayList<String>) attributes.get("isbns"));
        setAuthors((ArrayList<String>) attributes.get("authors"));
        setPublisher((String) attributes.get("publisher"));
        setImages((ArrayList<String>) attributes.get("images"));
        setAnnotation((String) attributes.get("annotation"));
        setSeries((String) attributes.get("series"));
    }
}
