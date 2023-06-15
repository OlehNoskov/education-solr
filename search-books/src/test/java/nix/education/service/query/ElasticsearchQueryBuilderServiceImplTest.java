package nix.education.service.query;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import nix.education.BaseTestClass;
import nix.education.service.query.elasticsearch.ElasticsearchQueryBuilderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static nix.education.data.TestConstants.AUTHOR;
import static nix.education.data.TestConstants.DESCRIPTION;
import static nix.education.data.TestConstants.TAGS;
import static nix.education.data.TestConstants.TEST_QUERY;
import static nix.education.data.TestConstants.TITLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElasticsearchQueryBuilderServiceImplTest extends BaseTestClass {

    @Autowired
    private ElasticsearchQueryBuilderService elasticsearchQueryBuilderService;

    @Nested
    @DisplayName("\"getQuery\" method should ")
    public class GetQueryTest {

        @Test
        @DisplayName("get query by query and empty filters")
        @SneakyThrows
        public void shouldGetQueryWhenProvidedQuery() {
            BoolQuery.Builder builder =
                elasticsearchQueryBuilderService.getQuery(TEST_QUERY, Optional.empty());
            String query = builder.build()._toQuery().toString();
            String expectedQuery = getExpectedQuery().build()._toQuery().toString();

            assertEquals(expectedQuery, query);
        }

        @Test
        @DisplayName("get elastic query by query and filters")
        @SneakyThrows
        public void shouldGetQueryWhenProvidedQueryAndTermFilters() {
            BoolQuery.Builder builder =
                elasticsearchQueryBuilderService.getQuery(TEST_QUERY,
                    Optional.of(Set.of("tags:test")));
            String query = builder.build()._toQuery().toString();
            List<Query> queries =
                List.of(QueryBuilders.term(f -> f.field(TAGS).value("test")));
            String expectedQuery = getExpectedQuery().filter(queries).build()._toQuery().toString();

            assertEquals(expectedQuery, query);
        }

        @Test
        @DisplayName("throw IllegalArgumentException")
        @SneakyThrows
        public void shouldThrowExceptionWhenFilterFieldIsInvalid() {
            assertThrows(IllegalArgumentException.class,
                () -> elasticsearchQueryBuilderService
                    .getQuery(TEST_QUERY, Optional.of(Set.of("test_field:query"))));
        }
    }

    private BoolQuery.Builder getExpectedQuery() {
        Map<String, Float> fields = new LinkedHashMap<>();
        fields.put(TITLE, 2.0F);
        fields.put(AUTHOR, 1.5F);
        fields.put(DESCRIPTION, 1.0F);
        fields.put(TAGS, 1.2F);
        List<Query> matchQueries = new ArrayList<>();
        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        for (Map.Entry<String, Float> entry : fields.entrySet()) {
            Query q = QueryBuilders
                .matchPhrase()
                .slop(3)
                .field(entry.getKey())
                .query(TEST_QUERY)
                .boost(entry.getValue())
                .build()._toQuery();
            matchQueries.add(q);
        }
        return boolQuery.should(matchQueries);
    }
}
