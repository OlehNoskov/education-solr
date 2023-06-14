package nix.education.service.response;


import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import nix.education.BaseTestClass;
import nix.education.entity.ElasticBook;
import nix.education.service.response.elasticsearch.ElasticsearchResponseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ElasticsearchResponseServiceTest extends BaseTestClass {

    @Autowired
    private ElasticsearchResponseService elasticsearchResponseService;

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
        @DisplayName("return message")
        @SneakyThrows
        public void shouldReturnMessageWhenBooksNotFound() {
            when(responseMock.hits()).thenReturn(hitsMetadataMock);
            when(hitsMetadataMock.hits()).thenReturn(listHitsMock);

            Map<String, Object> expected =
                Map.of("message", "Sorry, any documents weren't found!");

        }
    }
}
