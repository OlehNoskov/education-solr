package nix.education.service.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import nix.education.BaseTestClass;
import nix.education.entity.Book;
import nix.education.repository.BookRepository;
import nix.education.service.BookDataService;
import nix.education.service.query.solr.SolrQueryBuilderService;
import nix.education.service.response.solr.SolrResponseService;
import nix.education.service.suggestions.SuggestionService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;

import static nix.education.data.TestBookData.getBook;
import static nix.education.data.TestConstants.AUTHOR;
import static nix.education.data.TestConstants.FILTERS;
import static nix.education.data.TestConstants.TEST_QUERY;
import static nix.education.util.ResponseMessages.DOCUMENTS_NOT_FOUND_MESSAGE;
import static nix.education.util.ResponseMessages.SUGGESTIONS_NOT_FOUND_MESSAGE;
import static nix.education.util.SearchConstants.DOCS;
import static nix.education.util.SearchConstants.FACET_COUNTS;
import static nix.education.util.SearchConstants.NUM_FOUND;
import static nix.education.util.SearchConstants.SUGGEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SolrSearchServiceTest extends BaseTestClass {

    @Autowired
    @Qualifier("solrSearchService")
    private SearchService searchService;

    @MockBean
    @Qualifier("solrSuggestionService")
    private SuggestionService suggestionServiceMock;

    @MockBean
    private SolrResponseService responseServiceMock;

    @MockBean
    private BookDataService bookDataServiceMock;

    @MockBean
    private SolrClient solrClientMock;

    @MockBean
    private BookRepository bookRepositoryMock;

    @MockBean
    private SolrQueryBuilderService solrQueryBuilderServiceMock;

    @Mock
    private UpdateResponse updateResponseMock;

    @Mock
    private SolrQuery solrQueryMock;

    @Mock
    private QueryResponse queryResponseMock;

    @Nested
    @DisplayName("\"addBook\" method should ")
    public class AddBookTest {

        @Test
        @DisplayName("save book")
        @SneakyThrows
        public void shouldSaveBook() {
            Book book = getBook();

            doNothing().when(bookDataServiceMock).setTags(any());
            when(bookRepositoryMock.save(any())).thenReturn(book);
            when(solrClientMock.addBean(any())).thenReturn(updateResponseMock);
            when(solrClientMock.commit()).thenReturn(updateResponseMock);

            Book bookResult = searchService.addBook(book);

            verify(bookDataServiceMock).setTags(book);
            verify(bookRepositoryMock).save(book);
            verify(solrClientMock).addBean(book);
            verify(solrClientMock).commit();

            assertEquals(book, bookResult);
        }

        @Test
        @SneakyThrows
        @DisplayName("throw SolrServerException")
        public void shouldThrowExceptionWhenCommitToSolr() {
            Book book = getBook();
            when(bookRepositoryMock.save(any())).thenReturn(book);
            when(solrClientMock.commit()).thenThrow(SolrServerException.class);

            assertThrows(SolrServerException.class, () -> searchService.addBook(book));
        }
    }

    @Nested
    @DisplayName("\"deleteBook\" method should ")
    public class DeleteBookTest {

        @Test
        @DisplayName("delete book by id")
        @SneakyThrows
        public void shouldDeleteBookById() {
            long bookId = 1L;
            doNothing().when(bookRepositoryMock).deleteById(any());
            when(solrClientMock.deleteById(anyString())).thenReturn(updateResponseMock);
            when(solrClientMock.commit()).thenReturn(updateResponseMock);

            searchService.deleteBook(bookId);

            verify(bookRepositoryMock).deleteById(bookId);
            verify(solrClientMock).deleteById(String.valueOf(bookId));
            verify(solrClientMock).commit();
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
            List<Book> books = searchService.findAll();

            verify(bookRepositoryMock).findAll();
            assertEquals(expectedBooks, books);
        }

        @Test
        @DisplayName("not find all books")
        public void shouldNotFindAllBooks() {
            when(bookRepositoryMock.findAll()).thenReturn(Collections.emptyList());
            List<Book> books = searchService.findAll();

            verify(bookRepositoryMock).findAll();
            assertEquals(Collections.emptyList(), books);
        }
    }

    @Nested
    @DisplayName("\"findBooks\" method should ")
    public class FindBooksTest {

        @Test
        @DisplayName("find books by query")
        @SneakyThrows
        public void shouldFindBooksWhenProvidedQuery() {
            List<Book> books = List.of(getBook());
            Map<String, Object> expectedResponse = new LinkedHashMap<>();
            expectedResponse.put(NUM_FOUND, 1);
            expectedResponse.put(DOCS, books);
            expectedResponse.put(FACET_COUNTS, Collections.emptyMap());
            when(solrQueryBuilderServiceMock.getQuery(anyString(), any()))
                .thenReturn(solrQueryMock);
            when(solrClientMock.query(any())).thenReturn(queryResponseMock);
            when(responseServiceMock.prepareResponse(any())).thenReturn(expectedResponse);

            Map<String, Object> response = searchService.findBooks(TEST_QUERY, Optional.empty());

            verify(responseServiceMock).prepareResponse(any());
            assertEquals(expectedResponse, response);
        }

        @Test
        @DisplayName("find books by query and filters query")
        @SneakyThrows
        public void shouldFindBooksWhenProvidedQueryAndFilters() {
            List<Book> books = List.of(getBook());
            Map<String, Object> expectedResponse = new LinkedHashMap<>();
            expectedResponse.put(NUM_FOUND, 1);
            expectedResponse.put(DOCS, books);
            expectedResponse.put(FACET_COUNTS, Collections.emptyMap());

            when(solrQueryBuilderServiceMock.getQuery(anyString(), any()))
                .thenReturn(solrQueryMock);
            when(solrClientMock.query(any())).thenReturn(queryResponseMock);
            when(responseServiceMock.prepareResponse(any())).thenReturn(expectedResponse);

            Map<String, Object> response =
                searchService.findBooks(TEST_QUERY, Optional.of(Set.of(FILTERS)));

            verify(responseServiceMock).prepareResponse(any());
            assertEquals(response, expectedResponse);
        }


        @Test
        @DisplayName("not find books by query")
        @SneakyThrows
        public void shouldReturnMessageWhenBooksNotFound() {
            when(solrQueryBuilderServiceMock.getQuery(anyString(), any()))
                .thenReturn(solrQueryMock);
            when(solrClientMock.query(any())).thenReturn(queryResponseMock);
            when(responseServiceMock.prepareResponse(any())).thenReturn(DOCUMENTS_NOT_FOUND_MESSAGE);

            Map<String, Object> response = searchService.findBooks(TEST_QUERY, Optional.empty());

            verify(responseServiceMock).prepareResponse(any());
            assertEquals(DOCUMENTS_NOT_FOUND_MESSAGE, response);
        }
    }

    @Nested
    @DisplayName("\"getAuthorSuggestions\" method should ")
    public class GetAuthorSuggestionsTest {

        @Test
        @DisplayName("find suggestions by query")
        public void shouldFindSuggestionsWhenProvidedQuery() {
            Map<String, Object> expectedResponse = new HashMap<>();
            expectedResponse.put(SUGGEST, Collections.emptyMap());
            when(suggestionServiceMock.prepareAuthorSuggestionsResponse(anyString()))
                .thenReturn(expectedResponse);

            Map<String, Object> response = searchService.getAuthorSuggestions(AUTHOR);

            verify(suggestionServiceMock).prepareAuthorSuggestionsResponse(anyString());
            assertEquals(expectedResponse, response);
        }

        @Test
        @DisplayName("not find any suggestions by query")
        public void shouldReturnMessageWhenSuggestionsNotFound() {
            when(suggestionServiceMock.prepareAuthorSuggestionsResponse(anyString()))
                .thenReturn(SUGGESTIONS_NOT_FOUND_MESSAGE);

            Map<String, Object> response = searchService.getAuthorSuggestions(TEST_QUERY);

            verify(suggestionServiceMock).prepareAuthorSuggestionsResponse(anyString());
            assertEquals(SUGGESTIONS_NOT_FOUND_MESSAGE, response);
        }
    }

    @Nested
    @DisplayName("\"initDataBase\" method should ")
    public class InitDatabaseTest {

        @Test
        @DisplayName("init database")
        @SneakyThrows
        public void shouldInitDatabase() {
            String expectedMessage = "Core \"books\" was initialized successfully!";
            String message = searchService.initDataBase();

            assertEquals(expectedMessage, message);
        }

        @Test
        @DisplayName("not init database")
        @SneakyThrows
        public void shouldThrowExceptionWhenCommitToSolr() {
            when(bookRepositoryMock.findAll()).thenReturn(List.of(getBook()));
            when(solrClientMock.commit()).thenThrow(SolrServerException.class);

            assertThrows(SolrServerException.class, () -> searchService.initDataBase());
        }
    }
}
