package parser.entities;

import lombok.*;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookDescription {
    private String author;
    private String title;
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
}
