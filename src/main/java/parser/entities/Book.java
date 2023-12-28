package parser.entities;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Book<T extends BookDescription> implements BookInterface<T> {
    private T bookDescription;
    private List<BookForGroups<BookDescriptionForGroups>> booksOfAuthor;
    private List<BookForGroups<BookDescriptionForGroups>> booksOfSeries;
    private List<BookForGroups<BookDescriptionForGroups>> booksOfGenre;

    @Override
    public void initializeBook(T bookDescription) {
        this.bookDescription = bookDescription;
    }
}
