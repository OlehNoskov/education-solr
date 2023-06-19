package nix.education.service.response;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nix.education.BaseTestClass;
import nix.education.service.BookDataService;
import nix.education.service.response.solr.SolrResponseService;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
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


class SolrResponseServiceTest extends BaseTestClass {

    @Autowired
    private SolrResponseService solrResponseService;

    @MockBean
    private BookDataService bookDataServiceMock;

    @Mock
    private QueryResponse queryResponseMock;

    @Mock
    private SolrDocumentList documentListMock;

    @Nested
    @DisplayName("\"prepareResponse\" method should ")
    public class PrepareResponseTest {

        @Test
        @DisplayName("return correct response")
        public void shouldReturnResponseWhenFoundBooks() {
            when(queryResponseMock.getResults()).thenReturn(documentListMock);
            when(documentListMock.isEmpty()).thenReturn(false);
            when(documentListMock.size()).thenReturn(1);
            when(bookDataServiceMock.getBooks(anyList())).thenReturn(List.of(getBook()));

            Map<String, Object> expectedResponse = new LinkedHashMap<>();
            expectedResponse.put(NUM_FOUND, 1);
            expectedResponse.put(DOCS, List.of(getBook()));
            expectedResponse.put(FACET_COUNTS, Collections.emptyMap());
            Map<String, Object> response = solrResponseService.prepareResponse(queryResponseMock);

            assertEquals(expectedResponse, response);
        }

        @Test
        @DisplayName("return message")
        public void shouldReturnMessageWhenBooksNotFound() {
            when(queryResponseMock.getResults()).thenReturn(documentListMock);
            when(documentListMock.isEmpty()).thenReturn(true);

            Map<String, Object> response = solrResponseService.prepareResponse(queryResponseMock);

            assertEquals(DOCUMENTS_NOT_FOUND_MESSAGE, response);
        }
    }
}
