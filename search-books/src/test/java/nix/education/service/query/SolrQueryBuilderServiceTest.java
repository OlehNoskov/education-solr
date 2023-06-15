package nix.education.service.query;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import nix.education.BaseTestClass;
import nix.education.service.query.solr.SolrQueryBuilderService;
import nix.education.util.DateFormat;
import nix.education.util.SearchConstants;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static nix.education.data.TestConstants.AUTHOR;
import static nix.education.data.TestConstants.DESCRIPTION;
import static nix.education.data.TestConstants.TAGS;
import static nix.education.data.TestConstants.TEST_QUERY;
import static nix.education.data.TestConstants.TITLE;
import static nix.education.util.SearchConstants.Q;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolrQueryBuilderServiceTest extends BaseTestClass {

    @Autowired
    private SolrQueryBuilderService solrQueryBuilderService;

    private static final String PUBLICATION_DATE = "publication_date";

    @Nested
    @DisplayName("\"getQuery\" method should ")
    public class GetQueryTest {

        @Test
        @DisplayName("return solr query by query")
        public void shouldReturnQueryWhenProvidedQuery() {
            SolrQuery expectedSolrQuery = new SolrQuery();
            expectedSolrQuery.add(Q, getSearchQuery());
            expectedSolrQuery.addFacetField("tags_str");
            expectedSolrQuery.addDateRangeFacet(PUBLICATION_DATE,
                DateFormat.getStartSearchBookDate(),
                new Date(System.currentTimeMillis()),
                String.format("+%dYEAR", SearchConstants.DATE_RANGE_FACET_GAP));
            expectedSolrQuery.setFacetMinCount(1);

            SolrQuery solrQuery =
                solrQueryBuilderService.getQuery(TEST_QUERY, Optional.empty());

            assertEquals(expectedSolrQuery.getQuery(), solrQuery.getQuery());
            assertEquals(expectedSolrQuery.getFacetFields().length,
                solrQuery.getFacetFields().length);
            assertEquals(expectedSolrQuery.getFacetMinCount(),
                solrQuery.getFacetMinCount());
        }

        @Test
        @DisplayName("return solr query by query and filters")
        public void shouldReturnSolrQueryWhenProvidedQueryAndFilters() {
            SolrQuery expectedSolrQuery = new SolrQuery();
            expectedSolrQuery.add(Q, getSearchQuery());
            expectedSolrQuery.addFacetField("tags_str");
            expectedSolrQuery.addDateRangeFacet(PUBLICATION_DATE,
                DateFormat.getStartSearchBookDate(),
                new Date(System.currentTimeMillis()),
                String.format("+%dYEAR", SearchConstants.DATE_RANGE_FACET_GAP));

            SolrQuery solrQuery =
                solrQueryBuilderService.getQuery(TEST_QUERY, Optional.of(Set.of("tags:Drama")));

            assertEquals(expectedSolrQuery.getQuery(), solrQuery.getQuery());
            assertEquals(expectedSolrQuery.getFacetFields().length,
                solrQuery.getFacetFields().length);
            assertEquals(expectedSolrQuery.getFacetMinCount(),
                solrQuery.getFacetMinCount());
        }

        @Test
        @DisplayName("return incorrect solr query")
        public void shouldReturnIncorrectSolrQuery() {
            SolrQuery expectedSolrQuery = new SolrQuery();
            expectedSolrQuery.add(Q, getSearchQuery());
            expectedSolrQuery.addFacetField(TAGS);
            expectedSolrQuery.addDateRangeFacet(PUBLICATION_DATE,
                DateFormat.getStartSearchBookDate(),
                new Date(System.currentTimeMillis()),
                String.format("+%dYEAR", SearchConstants.DATE_RANGE_FACET_GAP));

            SolrQuery solrQuery =
                solrQueryBuilderService.getQuery(TEST_QUERY, Optional.empty());

            assertNotEquals(expectedSolrQuery, solrQuery);
        }

        @Test
        @DisplayName("throw exception")
        public void shouldThrowExceptionWhenQueryHasIncorrectField() {
            String expectedErrorMessage = "Invalid filter query: 'test_field'";
            try {
                solrQueryBuilderService.getQuery(TEST_QUERY,
                    Optional.of(Set.of("test_field:test")));
            } catch (RuntimeException e) {
                assertTrue(true);
                assertEquals(expectedErrorMessage, e.getMessage());
            }
        }
    }

    private String getSearchQuery() {
        return TITLE + ":" + TEST_QUERY + "^2.0 OR " +
            AUTHOR + ":" + TEST_QUERY + "^1.5 OR " +
            DESCRIPTION + ":" + TEST_QUERY + "^1.0 OR " +
            TAGS + ":" + TEST_QUERY + "^1.2";
    }
}
