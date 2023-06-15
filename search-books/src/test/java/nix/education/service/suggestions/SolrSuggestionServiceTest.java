package nix.education.service.suggestions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import nix.education.BaseTestClass;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class SolrSuggestionServiceTest extends BaseTestClass {

    @Autowired
    @Qualifier("solrSuggestionService")
    private SuggestionService suggestionService;

    @MockBean
    private SolrClient solrClientMock;

    @Mock
    private QueryResponse queryResponseMock;

    @Mock
    private SuggesterResponse suggesterResponseMock;

    @Mock
    private Map<String, List<String>> mapMock;

    @Nested
    @DisplayName("\"prepareAuthorSuggestionsResponse\" method should ")
    public class PrepareAuthorSuggestionsResponseTest {

        @Test
        @DisplayName("return author suggestions")
        @SneakyThrows
        public void shouldReturnAuthorSuggestions() {
            when(solrClientMock.query(any())).thenReturn(queryResponseMock);
            when(queryResponseMock.getSuggesterResponse()).thenReturn(suggesterResponseMock);
            when(suggesterResponseMock.getSuggestedTerms()).thenReturn(mapMock);
            when(suggesterResponseMock.getSuggestedTerms().get(anyString()))
                .thenReturn(List.of(AUTHOR));

            Map<String, Object> suggestions = new LinkedHashMap<>();
            suggestions.put(NUM_FOUND, 1);
            suggestions.put(SUGGESTIONS, List.of(AUTHOR));
            Map<String, Object> expectedResponse = new LinkedHashMap<>();
            expectedResponse.put(SUGGEST,
                Map.of(AUTHOR_SUGGESTER, Map.of(TEST_QUERY, suggestions)));

            Map<String, Object> response =
                suggestionService.prepareAuthorSuggestionsResponse(TEST_QUERY);

            assertEquals(expectedResponse, response);
        }

        @Test
        @SneakyThrows
        @DisplayName("return message")
        public void shouldReturnMessageWhenMapIsEmpty() {
            when(solrClientMock.query(any())).thenReturn(queryResponseMock);
            when(queryResponseMock.getSuggesterResponse()).thenReturn(suggesterResponseMock);
            when(suggesterResponseMock.getSuggestedTerms()).thenReturn(mapMock);
            when(suggesterResponseMock.getSuggestedTerms().get(anyString()))
                .thenReturn(Collections.emptyList());

            Map<String, Object> response =
                suggestionService.prepareAuthorSuggestionsResponse(TEST_QUERY);

            assertEquals(SUGGESTIONS_NOT_FOUND_MESSAGE, response);
        }
    }
}
