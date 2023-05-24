package nix.education.service.query;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import lombok.SneakyThrows;
import nix.education.exception.InvalidFilterQueryException;
import nix.education.util.FacetFields;
import nix.education.util.SymbolConstants;
import nix.education.util.SymbolsSolrQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static nix.education.util.DateFormat.DATE_FORMAT;


@Service
public class SolrQueryBuilderService {

    @Value("${search.term-facets}")
    private Set<String> termFacets;

    @Value("${search.range-facets}")
    private Set<String> rangeFacet;

    @Value("#{${search.search-fields}}")
    private Map<String, Float> fields;

    private final Date START_SEARCH_BOOK_DATE;

    @SneakyThrows
    public SolrQueryBuilderService() {
        this.START_SEARCH_BOOK_DATE = DATE_FORMAT.parse("1899-12-31 22:00:00");
    }

    public SolrQuery getSolrQuery(String q, Optional<Set<String>> fq) {
        SolrQuery query = new SolrQuery();
        query.add(SymbolsSolrQuery.QUERY, buildSearchQuery(q));
        termFacets.forEach(query::addFacetField);
        query.addDateRangeFacet("publication_date", START_SEARCH_BOOK_DATE,
            new Date(System.currentTimeMillis()),
            String.format("+%dYEAR", FacetFields.DATE_RANGE_FACET_GAP));
        addFiltersToQuery(query, fq);

        return query;
    }

    private String buildSearchQuery(String q) {
        StringJoiner stringJoiner = new StringJoiner(SymbolsSolrQuery.OR);
        for (Map.Entry<String, Float> field : fields.entrySet()) {
            stringJoiner.add(field.getKey() +
                SymbolConstants.COLON + q + SymbolConstants.CIRCUMFLEX + field.getValue());
        }
        return stringJoiner.toString();
    }

    private void addFiltersToQuery(SolrQuery query, Optional<Set<String>> fq) {
        query.setFacetMinCount(1);
        if (fq.isPresent()) {
            Set<String> validatedFQ = validateFilterQuery(fq);
            for (String filter : validatedFQ) {
                query.addFilterQuery(filter);
            }
        }
    }

    private Set<String> validateFilterQuery(Optional<Set<String>> filters) {
        if (filters.isPresent()) {
            for (String filter : filters.get()) {
                String facetField = StringUtils.substringBefore(filter, SymbolConstants.COLON);
                if (!termFacets.contains(facetField) && !rangeFacet.contains(facetField)) {
                    throw new InvalidFilterQueryException(facetField);
                }
            }
        }
        return filters.get();
    }
}
