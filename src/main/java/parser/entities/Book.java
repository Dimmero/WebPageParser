package parser.entities;

import lombok.*;
import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Book<T extends BookDescription> implements BookInterface<T> {
    private T bookDescription;
    private ArrayList<BookForGroups<BookDescriptionForGroups>> booksOfAuthor;
    private ArrayList<BookForGroups<BookDescriptionForGroups>> booksOfSeries;
    private ArrayList<BookForGroups<BookDescriptionForGroups>> booksOfGenre;

    @Override
    public void initializeBook(T bookDescription) {
        this.bookDescription = bookDescription;
    }
}
