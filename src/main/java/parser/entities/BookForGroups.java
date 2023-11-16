package parser.entities;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookForGroups implements BookInterface {
    private BookDescriptionForGroups bookDescriptionForGroups;

    @Override
    public void initializeRelatedBook(BookDescriptionForGroups bookDescriptionForGroups) {
        this.bookDescriptionForGroups = bookDescriptionForGroups;
    }

    @Override
    public void initializeMainBook(BookDescription bookDescription) {

    }
}
