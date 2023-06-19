package nix.education.repository;

import java.util.List;
import java.util.Optional;
import nix.education.BaseTestClass;
import nix.education.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static nix.education.data.TestBookData.getBook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookRepositoryTest extends BaseTestClass {

    @MockBean
    private BookRepository bookRepositoryMock;

    @Nested
    @DisplayName("\"save\" method should ")
    public class SaveBookTest {

        @Test
        @DisplayName("save new book")
        public void shouldAddNewBook() {
            Book expectedBook = getBook();
            when(bookRepositoryMock.save(expectedBook)).thenReturn(expectedBook);
            Book book = bookRepositoryMock.save(expectedBook);

            assertEquals(expectedBook, book);
        }
    }

    @Nested
    @DisplayName("\"findById\" method should ")
    public class FindByIdTest {

        @Test
        @DisplayName("find book by id")
        public void shouldFindBookById() {
            Book expectedBook = getBook();
            when(bookRepositoryMock.findById(1L)).thenReturn(Optional.of(expectedBook));
            Book book = bookRepositoryMock.findById(1L).get();

            assertEquals(expectedBook, book);
        }
    }

    @Nested
    @DisplayName("\"delete\" method should ")
    public class DeleteBookTest {

        @Test
        @DisplayName("delete book")
        public void shouldDeleteBook() {
            Book book = getBook();
            doNothing().when(bookRepositoryMock).delete(book);
            bookRepositoryMock.delete(book);

            verify(bookRepositoryMock).delete(book);
        }

        @Test
        @DisplayName("delete book by id")
        public void shouldDeleteBookById() {
            Book book = getBook();
            doNothing().when(bookRepositoryMock).deleteById(book.getId());
            bookRepositoryMock.deleteById(book.getId());

            verify(bookRepositoryMock).deleteById(book.getId());
        }
    }

    @Nested
    @DisplayName("\"findAll\" method should ")
    public class FindAllTest {

        @Test
        @DisplayName("find all books")
        public void shouldFindAllBooks() {
            List<Book> expectedBooks = List.of(getBook());
            when(bookRepositoryMock.findAll()).thenReturn(expectedBooks);
            List<Book> books = (List<Book>) bookRepositoryMock.findAll();

            assertEquals(expectedBooks, books);
        }
    }
}
