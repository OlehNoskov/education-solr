package nix.education.data;

import java.util.Date;
import java.util.List;
import nix.education.entity.Book;
import nix.education.entity.Tag;

import static nix.education.data.TestConstants.AUTHOR;
import static nix.education.data.TestConstants.DESCRIPTION;
import static nix.education.data.TestConstants.TEST_TAG;
import static nix.education.data.TestConstants.TITLE;


public class TestBookData {

    private static Book book = setupBook();

    public static Book getBook() {
        return book;
    }

    private static Book setupBook() {
        book = new Book();
        book.setId(1L);
        book.setAuthor(AUTHOR);
        book.setTitle(TITLE);
        book.setDescription(DESCRIPTION);
        book.setPublicationDate(new Date(System.currentTimeMillis()));
        Tag tag = new Tag();
        tag.setTag(TEST_TAG);
        tag.setId(1L);
        book.setTags(List.of(tag));
        return book;
    }
}
