package parser.entities;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookForGroups<T extends BookDescriptionInterface> implements BookInterface<T> {
    private T bookDescription;

    @Override
    public void initializeBook(T bookDescription) {
        this.bookDescription = bookDescription;
    }
}
