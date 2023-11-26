package parser.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookDescriptionForGroups implements BookDescriptionInterface {
    private String bookId;
    private ArrayList<String> isbns;

    @Override
    public void initializeDescriptionForBook(Map<String, Object> attributes) {
        setBookId((String) attributes.get("bookId"));
        setIsbns((ArrayList<String>) attributes.get("isbns"));
    }
}
