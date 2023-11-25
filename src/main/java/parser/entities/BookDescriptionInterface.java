package parser.entities;

import java.util.ArrayList;

public interface BookDescriptionInterface {
    void initializeDescriptionForMainBook(String author, String title, String annotation, String publisher, String series, String bookId, ArrayList<String> isbns, ArrayList<String> images);
    void initializeDescriptionForSectionGroup(String bookId, ArrayList<String> isbns);
}
