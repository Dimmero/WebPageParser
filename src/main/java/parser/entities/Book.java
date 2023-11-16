package parser.entities;

import lombok.*;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Book {
    private BookDescription bookDescription;
    private ArrayList<BookForGroups> booksOfAuthor;
    private ArrayList<BookForGroups> booksOfSeries;
    private ArrayList<BookForGroups> booksOfGenre;

}
