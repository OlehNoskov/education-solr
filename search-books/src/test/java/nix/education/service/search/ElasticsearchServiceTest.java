package nix.education.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.SneakyThrows;
import nix.education.BaseTestClass;
import nix.education.entity.Book;
import nix.education.entity.ElasticBook;
import nix.education.repository.BookRepository;
import nix.education.service.BookDataService;
import nix.education.service.query.elasticsearch.ElasticsearchQueryBuilderService;
import nix.education.service.response.elasticsearch.ElasticsearchResponseService;
import nix.education.service.suggestions.SuggestionService;
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
import static nix.education.util.SearchConstants.AUTHOR_SUGGESTER;
import static nix.education.util.SearchConstants.DOCS;
import static nix.education.util.SearchConstants.FACET_COUNTS;
import static nix.education.util.SearchConstants.NUM_FOUND;
import static nix.education.util.SearchConstants.SUGGEST;
import static nix.education.util.SearchConstants.SUGGESTIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElasticsearchServiceTest extends BaseTestClass {

    @Autowired
    @Qualifier("elasticsearchService")
    private SearchService searchService;

    @MockBean
    @Qualifier("elasticsearchSuggestionService")
    private SuggestionService suggestionServiceMock;

    @MockBean
    private ElasticsearchResponseService responseServiceMock;

    @MockBean
    private BookDataService bookDataServiceMock;

    @MockBean
    private ElasticsearchQueryBuilderService queryBuilderServiceMock;

    @MockBean
    private BookRepository bookRepositoryMock;

    @MockBean
    private ElasticsearchClient elasticsearchClientMock;

    @Mock
    private DeleteResponse deleteResponseMock;

    @Mock
    private IndexResponse indexResponseMock;

    @Mock
    private BoolQuery.Builder boolQueryBuilderMock;

    @Mock
    private SearchRequest searchRequestMock;

    @Mock
    private SearchResponse<ElasticBook> searchResponseMock;

    @Mock
    private SearchRequest.Builder searchRequestBuilderMock;

    @Mock
    private BoolQuery boolQueryMock;

    @Mock
    private Query queryMock;

    @Nested
    @DisplayName("\"addBook\" method should ")
    public class AddBookTest {

        @Test
        @DisplayName("save book")
        @SneakyThrows
        public void shouldSaveBook() {
            Book expectedBook = getBook();
            doNothing().when(bookDataServiceMock).setTags(expectedBook);
            when(bookRepositoryMock.save(expectedBook)).thenReturn(expectedBook);
            when(elasticsearchClientMock.index(any(Function.class))).thenReturn(indexResponseMock);

            Book bookResult = searchService.addBook(expectedBook);

            verify(bookDataServiceMock).setTags(expectedBook);
            verify(bookRepositoryMock).save(expectedBook);
            verify(elasticsearchClientMock).index(any(Function.class));
            assertEquals(expectedBook, bookResult);
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
            doNothing().when(bookRepositoryMock).deleteById(bookId);
            when(elasticsearchClientMock.delete(any(DeleteRequest.class)))
                .thenReturn(deleteResponseMock);

            searchService.deleteBook(bookId);

            verify(bookRepositoryMock).deleteById(bookId);
            verify(elasticsearchClientMock).delete(any(DeleteRequest.class));
        }
    }

    @Nested
    @DisplayName("\"findAll\" method should ")
    public class FindAllTest {
        @Test
        @DisplayName("find all books")
        @SneakyThrows
        public void shouldFindAllBooks() {
            List<Book> expectedBooks = List.of(getBook());
            when(bookRepositoryMock.findAll()).thenReturn(expectedBooks);
            List<Book> books = searchService.findAll();

            verify(bookRepositoryMock).findAll();
            assertEquals(expectedBooks, books);
        }

        @Test
        @DisplayName("not find all books")
        @SneakyThrows
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

            when(queryBuilderServiceMock.getQuery(anyString(), any()))
                .thenReturn(boolQueryBuilderMock);
            when(searchRequestBuilderMock.index(anyList()))
                .thenReturn(searchRequestBuilderMock);
            when(boolQueryBuilderMock.build()).thenReturn(boolQueryMock);
            when(boolQueryMock._toQuery()).thenReturn(queryMock);
            when(searchRequestBuilderMock.aggregations(anyMap()))
                .thenReturn(searchRequestBuilderMock);
            when(searchRequestBuilderMock.build()).thenReturn(searchRequestMock);
            when(elasticsearchClientMock.search(any(SearchRequest.class), eq(ElasticBook.class)))
                .thenReturn(searchResponseMock);
            when(responseServiceMock.prepareResponse(any())).thenReturn(expectedResponse);

            Map<String, Object> response = searchService.findBooks(TEST_QUERY, Optional.empty());

            assertEquals(expectedResponse, response);
        }

        @Test
        @DisplayName("find books by query and filters")
        @SneakyThrows
        public void shouldFindBooksWhenProvidedQueryAndFilters() {
            List<Book> books = List.of(getBook());
            Map<String, Object> expectedResponse = new LinkedHashMap<>();
            expectedResponse.put(NUM_FOUND, 1);
            expectedResponse.put(DOCS, books);
            expectedResponse.put(FACET_COUNTS, Collections.emptyMap());

            when(queryBuilderServiceMock.getQuery(anyString(), any()))
                .thenReturn(boolQueryBuilderMock);
            when(searchRequestBuilderMock.index(anyList()))
                .thenReturn(searchRequestBuilderMock);
            when(boolQueryBuilderMock.build()).thenReturn(boolQueryMock);
            when(boolQueryMock._toQuery()).thenReturn(queryMock);
            when(searchRequestBuilderMock.aggregations(anyMap()))
                .thenReturn(searchRequestBuilderMock);
            when(searchRequestBuilderMock.build()).thenReturn(searchRequestMock);
            when(elasticsearchClientMock.search(any(SearchRequest.class), eq(ElasticBook.class)))
                .thenReturn(searchResponseMock);
            when(responseServiceMock.prepareResponse(any())).thenReturn(expectedResponse);

            Map<String, Object> response =
                searchService.findBooks(TEST_QUERY, Optional.of(Set.of(FILTERS)));

            assertEquals(expectedResponse, response);
        }


        @Test
        @DisplayName("not find books by query")
        @SneakyThrows
        public void shouldNotFindAnyBooksWhenProvidedQuery() {
            when(queryBuilderServiceMock.getQuery(anyString(), any()))
                .thenReturn(boolQueryBuilderMock);
            when(searchRequestBuilderMock.index(anyList()))
                .thenReturn(searchRequestBuilderMock);
            when(boolQueryBuilderMock.build()).thenReturn(boolQueryMock);
            when(boolQueryMock._toQuery()).thenReturn(queryMock);
            when(searchRequestBuilderMock.aggregations(anyMap()))
                .thenReturn(searchRequestBuilderMock);
            when(searchRequestBuilderMock.build()).thenReturn(searchRequestMock);
            when(elasticsearchClientMock.search(any(SearchRequest.class), eq(ElasticBook.class)))
                .thenReturn(searchResponseMock);
            when(responseServiceMock.prepareResponse(any()))
                .thenReturn(DOCUMENTS_NOT_FOUND_MESSAGE);

            Map<String, Object> response = searchService.findBooks(TEST_QUERY, Optional.empty());

            assertEquals(DOCUMENTS_NOT_FOUND_MESSAGE, response);
        }
    }

    @Nested
    @DisplayName("\"getAuthorSuggestions\" method should ")
    public class GetAuthorSuggestionsTest {

        @Test
        @DisplayName("find suggestions by query")
        @SneakyThrows
        public void shouldFindSuggestionsWhenProvidedQuery() {
            Map<String, Object> suggestions = new LinkedHashMap<>();
            suggestions.put(NUM_FOUND, 1);
            suggestions.put(SUGGESTIONS, List.of(AUTHOR));
            Map<String, Object> expectedResponse = new LinkedHashMap<>();
            expectedResponse.put(SUGGEST,
                Map.of(AUTHOR_SUGGESTER, Map.of(TEST_QUERY, suggestions)));

            when(suggestionServiceMock.prepareAuthorSuggestionsResponse(anyString()))
                .thenReturn(expectedResponse);

            Map<String, Object> response = searchService.getAuthorSuggestions(AUTHOR);

            verify(suggestionServiceMock).prepareAuthorSuggestionsResponse(AUTHOR);
            assertEquals(expectedResponse, response);
        }

        @Test
        @DisplayName("not find any suggestions by query and return message")
        @SneakyThrows
        public void shouldReturnMessageWhenSuggestionsNotFound() {
            when(suggestionServiceMock.prepareAuthorSuggestionsResponse(TEST_QUERY))
                .thenReturn(SUGGESTIONS_NOT_FOUND_MESSAGE);

            Map<String, Object> response = searchService.getAuthorSuggestions(TEST_QUERY);

            verify(suggestionServiceMock).prepareAuthorSuggestionsResponse(TEST_QUERY);
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
            String expectedMessage = "Index \"books\" was initialized successfully!";
            String message = searchService.initDataBase();

            assertEquals(expectedMessage, message);
        }
    }
}
