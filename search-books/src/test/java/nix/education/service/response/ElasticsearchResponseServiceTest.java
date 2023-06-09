package nix.education.service.response;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nix.education.BaseTestClass;
import nix.education.entity.ElasticBook;
import nix.education.service.BookDataService;
import nix.education.service.response.elasticsearch.ElasticsearchResponseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static nix.education.data.TestBookData.getBook;
import static nix.education.util.ResponseMessages.DOCUMENTS_NOT_FOUND_MESSAGE;
import static nix.education.util.SearchConstants.DOCS;
import static nix.education.util.SearchConstants.FACET_COUNTS;
import static nix.education.util.SearchConstants.NUM_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

public class ElasticsearchResponseServiceTest extends BaseTestClass {

    @Autowired
    private ElasticsearchResponseService elasticsearchResponseService;

    @MockBean
    private BookDataService bookDataServiceMock;

    @Mock
    private SearchResponse<ElasticBook> responseMock;

    @Mock
    private HitsMetadata<ElasticBook> hitsMetadataMock;

    @Mock
    private List<Hit<ElasticBook>> listHitsMock;

    @Nested
    @DisplayName("\"getQuery\" method should ")
    public class PrepareResponseTest {

        @Test
        @DisplayName("return response")
        public void shouldReturnResponseWhenBooksFound() {
            when(responseMock.hits()).thenReturn(hitsMetadataMock);
            when(hitsMetadataMock.hits()).thenReturn(listHitsMock);
            when(hitsMetadataMock.hits().isEmpty()).thenReturn(false);
            when(hitsMetadataMock.hits().size()).thenReturn(1);
            when(bookDataServiceMock.getBooks(anyList())).thenReturn(List.of(getBook()));

            Map<String, Object> expectedResponse = new LinkedHashMap<>();
            expectedResponse.put(NUM_FOUND, 1);
            expectedResponse.put(DOCS, List.of(getBook()));
            expectedResponse.put(FACET_COUNTS, Collections.emptyMap());
            Map<String, Object> response =
                elasticsearchResponseService.prepareResponse(responseMock);

            assertEquals(expectedResponse, response);
        }

        @Test
        @DisplayName("return message")
        public void shouldReturnMessageWhenBooksNotFound() {
            when(responseMock.hits()).thenReturn(hitsMetadataMock);
            when(hitsMetadataMock.hits()).thenReturn(listHitsMock);
            when(hitsMetadataMock.hits()).thenReturn(Collections.emptyList());

            Map<String, Object> result = elasticsearchResponseService.prepareResponse(responseMock);

            assertEquals(DOCUMENTS_NOT_FOUND_MESSAGE, result);
        }
    }
}
