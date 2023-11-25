package parser.entities;

public interface BookInterface<T extends BookDescriptionInterface> {
    void initializeBook(T bookDescription);
}
