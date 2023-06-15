package nix.education.service.suggestions;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.elasticsearch.core.search.TermSuggest;
import co.elastic.clients.elasticsearch.core.search.TermSuggestOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import nix.education.BaseTestClass;
import nix.education.entity.ElasticBook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;

import static nix.education.data.TestConstants.AUTHOR;
import static nix.education.data.TestConstants.TEST_QUERY;
import static nix.education.util.ResponseMessages.SUGGESTIONS_NOT_FOUND_MESSAGE;
import static nix.education.util.SearchConstants.AUTHOR_SUGGESTER;
import static nix.education.util.SearchConstants.NUM_FOUND;
import static nix.education.util.SearchConstants.SUGGEST;
import static nix.education.util.SearchConstants.SUGGESTIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ElasticsearchSuggestionServiceTest extends BaseTestClass {

    @Autowired
    @Qualifier("elasticsearchSuggestionService")
    private SuggestionService suggestionService;

    @MockBean
    private ElasticsearchClient elasticsearchClientMock;

    @Mock
    private SearchResponse<ElasticBook> responseMock;

    @Mock
    private Map<String, List<Suggestion<ElasticBook>>> mapMock;

    @Mock
    private List<Suggestion<ElasticBook>> suggestionsMock;

    @Mock
    private Suggestion<ElasticBook> suggestionMock;

    @Mock
    private TermSuggest termSuggestMock;

    @Nested
    @DisplayName("\"prepareAuthorSuggestionsResponse\" method should ")
    public class PrepareAuthorSuggestionsResponseTest {

        @Test
        @DisplayName("return author suggestions")
        @SneakyThrows
        public void shouldReturnAuthorSuggestions() {
            Map<String, Object> expectedResponse = new LinkedHashMap<>();
            Map<String, Object> suggestions = new LinkedHashMap<>();
            suggestions.put(NUM_FOUND, 1);
            suggestions.put(SUGGESTIONS, List.of(AUTHOR));
            expectedResponse.put(SUGGEST, Map.of(AUTHOR_SUGGESTER,
                Map.of(TEST_QUERY, suggestions)));
            TermSuggestOption termSuggestOption =
                TermSuggestOption.of(t -> t.text(AUTHOR).score(1.0).freq(1L));

            when(elasticsearchClientMock.search(any(SearchRequest.class), eq(ElasticBook.class)))
                .thenReturn(responseMock);
            when(responseMock.suggest()).thenReturn(mapMock);
            when(mapMock.get(anyString())).thenReturn(suggestionsMock);
            when(suggestionsMock.get(anyInt())).thenReturn(suggestionMock);
            when(suggestionMock.term()).thenReturn(termSuggestMock);
            when(termSuggestMock.options()).thenReturn(List.of(termSuggestOption));

            Map<String, Object> response =
                suggestionService.prepareAuthorSuggestionsResponse(TEST_QUERY);

            assertEquals(expectedResponse, response);
        }

        @Test
        @SneakyThrows
        @DisplayName("return message")
        public void shouldReturnMessageWhenMapIsEmpty() {
            when(elasticsearchClientMock.search(any(SearchRequest.class), eq(ElasticBook.class)))
                .thenReturn(responseMock);
            when(responseMock.suggest()).thenReturn(mapMock);
            when(mapMock.get(anyString())).thenReturn(suggestionsMock);
            when(suggestionsMock.get(anyInt())).thenReturn(suggestionMock);
            when(suggestionMock.term()).thenReturn(termSuggestMock);
            when(termSuggestMock.options()).thenReturn(Collections.emptyList());

            Map<String, Object> response =
                suggestionService.prepareAuthorSuggestionsResponse(TEST_QUERY);

            assertEquals(SUGGESTIONS_NOT_FOUND_MESSAGE, response);
        }
    }
}
