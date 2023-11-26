package parser.entities;

import lombok.*;
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
        setAuthors((ArrayList<String>) attributes.get("authors"));
        setTitle((String) attributes.get("title"));
        setAnnotation((String) attributes.get("annotation"));
        setPublisher((String) attributes.get("publisher"));
        setSeries((String) attributes.get("series"));
        setBookId((String) attributes.get("bookId"));
        setIsbns((ArrayList<String>) attributes.get("isbns"));
        setImages((ArrayList<String>) attributes.get("images"));
    }
}
