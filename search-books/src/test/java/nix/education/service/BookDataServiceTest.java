package nix.education.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nix.education.BaseTestClass;
import nix.education.entity.Book;
import nix.education.entity.Tag;
import nix.education.repository.BookRepository;
import nix.education.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static nix.education.data.TestBookData.getBook;
import static nix.education.data.TestConstants.TEST_TAG;
import static nix.education.data.TestTagData.getTag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookDataServiceTest extends BaseTestClass {

    @Autowired
    private BookDataService bookDataService;

    @MockBean
    private TagRepository tagRepositoryMock;

    @MockBean
    private BookRepository bookRepositoryMock;

    @Nested
    @DisplayName("\"setTags\" method should ")
    public class SetTagsTest {

        @Test
        @DisplayName("set up correct tags")
        public void shouldCorrectSetUpTags() {
            Book book = getBook();

            when(tagRepositoryMock.findTagsByTagIn(anyList())).thenReturn(Collections.emptyList());
            bookDataService.setTags(book);
            List<Tag> expectedTagTitles = List.of(getTag());

            verify(tagRepositoryMock).findTagsByTagIn(List.of(TEST_TAG));
            assertEquals(expectedTagTitles, book.getTags());
        }
    }

    @Nested
    @DisplayName("\"getTagTitles\" method should ")
    public class GetTagTitlesTest {

        @Test
        @DisplayName("return correct list of tag titles")
        public void shouldReturnListOfTagTitles() {
            Book book = getBook();
            List<String> expectedTagTitles = List.of(TEST_TAG);
            List<String> tagTitles = bookDataService.getTagTitles(book);

            assertEquals(expectedTagTitles, tagTitles);
        }

        @Test
        @DisplayName("return not correct list of tag titles")
        public void shouldReturnNotCorrectListOfTagTitles() {
            Book book = getBook();
            List<String> expectedTagTitles = List.of(TEST_TAG + 1, TEST_TAG + 2);
            List<String> tagTitles = bookDataService.getTagTitles(book);

            assertNotEquals(expectedTagTitles, tagTitles);
        }
    }

    @Nested
    @DisplayName("\"getBooks\" method should ")
    public class GetBooksTest {

        @Test
        @DisplayName("return sorted books by ids")
        public void shouldReturnSortedBooks() {
            List<Long> ids = List.of(2L, 1L);
            Book firstBook = new Book();
            firstBook.setId(1L);
            Book secondBook = new Book();
            secondBook.setId(2L);
            List<Book> books = new ArrayList<>();
            books.add(firstBook);
            books.add(secondBook);

            when(bookRepositoryMock.findBooksByIdIn(anyList())).thenReturn(books);

            List<Book> expectedBooks = List.of(secondBook, firstBook);
            List<Book> sortedBooks = bookDataService.getBooks(ids);

            verify(bookRepositoryMock).findBooksByIdIn(ids);
            assertEquals(expectedBooks, sortedBooks);
        }


        @Test
        @DisplayName("return not sorted books by ids")
        public void shouldReturnNotSortedBooks() {
            List<Long> ids = List.of(1L, 2L);
            Book firstBook = new Book();
            firstBook.setId(1L);
            Book secondBook = new Book();
            secondBook.setId(2L);
            List<Book> books = new ArrayList<>();
            books.add(secondBook);
            books.add(firstBook);

            when(bookRepositoryMock.findBooksByIdIn(anyList())).thenReturn(books);

            List<Book> expectedBooks = List.of(secondBook, firstBook);
            List<Book> sortedBooks = bookDataService.getBooks(ids);

            verify(bookRepositoryMock).findBooksByIdIn(ids);
            assertNotEquals(expectedBooks, sortedBooks);
        }
    }
}
