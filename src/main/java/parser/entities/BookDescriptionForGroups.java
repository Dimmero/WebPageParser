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
public class BookDescriptionForGroups {
    private String bookId;
    private ArrayList<String> isbns;
}
