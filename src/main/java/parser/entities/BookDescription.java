package parser.entities;

import lombok.*;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookDescription implements BookDescriptionInterface {
    private String author;
    private String title;
    private String annotation;
    private String publisher;
    private String series;
    private String bookId;
    private ArrayList<String> isbns;
    private ArrayList<String> images;

    @Override
    public String toString() {
        return "BookDescription: {\n" +
                "author:" + author + "\n" +
                "title:" + title + "\n" +
                "publisher:" + publisher + "\n" +
                "series:" + series + "\n" +
                "bookId:" + bookId + "\n" +
                "isbn:" + isbns + "\n" +
                "images:" + images + "\n" +
                "}";
    }

    @Override
    public void initializeDescriptionForMainBook(String author, String title, String annotation, String publisher, String series, String bookId, ArrayList<String> isbns, ArrayList<String> images) {
        setAuthor(author);
        setTitle(title);
        setAnnotation(annotation);
        setPublisher(publisher);
        setSeries(series);
        setBookId(bookId);
        setIsbns(isbns);
        setImages(images);
    }

    @Override
    public void initializeDescriptionForSectionGroup(String bookId, ArrayList<String> isbns) {
    }
}
