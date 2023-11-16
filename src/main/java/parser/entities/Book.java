package parser.entities;

import lombok.*;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Book implements BookInterface{
    private BookDescription bookDescription;
    private ArrayList<BookForGroups> booksOfAuthor;
    private ArrayList<BookForGroups> booksOfSeries;
    private ArrayList<BookForGroups> booksOfGenre;

    @Override
    public void initializeRelatedBook(BookDescriptionForGroups bookDescriptionForGroups) {
    }

    @Override
    public void initializeMainBook(BookDescription bookDescription) {
        this.bookDescription = bookDescription;
    }
}
