package parser.entities;

import lombok.*;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BooksOfAuthor {
    private ArrayList<BookForGroups> booksOfAuthor;
}
