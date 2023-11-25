package parser.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookDescriptionForGroups implements BookDescriptionInterface {
    private String bookId;
    private ArrayList<String> isbns;

    @Override
    public void initializeDescriptionForMainBook(String author, String title, String annotation, String publisher, String series, String bookId, ArrayList<String> isbns, ArrayList<String> images) {
    }

    @Override
    public void initializeDescriptionForSectionGroup(String bookId, ArrayList<String> isbns) {
        setBookId(bookId);
        setIsbns(isbns);
    }
}
