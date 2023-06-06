package nix.education.service.query.solr;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import nix.education.util.DateFormat;
import nix.education.util.SearchConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SolrQueryBuilderServiceImpl implements SolrQueryBuilderService {

    @Value("${search.term-facets}")
    private Set<String> termFacets;

    @Value("${search.range-facets}")
    private Set<String> rangeFacets;

    @Value("#{${search.search-fields}}")
    private Map<String, Float> fields;

    private static final int FACET_MIN_COUNT = 1;

    @Override
    public SolrQuery getQuery(String q, Optional<Set<String>> fq) {
        SolrQuery query = new SolrQuery();
        query.add(SearchConstants.QUERY, buildSearchQuery(q));
        termFacets.forEach(query::addFacetField);
        query.addDateRangeFacet("publication_date", DateFormat.getStartSearchBookDate(),
            new Date(System.currentTimeMillis()),
            String.format("+%dYEAR", SearchConstants.DATE_RANGE_FACET_GAP));
        addFiltersToQuery(query, fq);

        return query;
    }

    private String buildSearchQuery(String q) {
        StringJoiner stringJoiner = new StringJoiner(SearchConstants.OR);
        for (Map.Entry<String, Float> field : fields.entrySet()) {
            stringJoiner.add(StringUtils.join(field.getKey(),
                SearchConstants.COLON, q, SearchConstants.CIRCUMFLEX, field.getValue()));
        }
        return stringJoiner.toString();
    }

    private void addFiltersToQuery(SolrQuery query, Optional<Set<String>> fq) {
        query.setFacetMinCount(FACET_MIN_COUNT);
        if (fq.isPresent()) {
            Set<String> validatedFQ = validateFilterQuery(fq);
            for (String filter : validatedFQ) {
                query.addFilterQuery(filter);
            }
        }
    }

    private Set<String> validateFilterQuery(Optional<Set<String>> filters) {
            for (String filter : filters.get()) {
                String facetField = StringUtils.substringBefore(filter, SearchConstants.COLON);
                if (!termFacets.contains(facetField) && !rangeFacets.contains(facetField)) {
                    throw new RuntimeException(
                        String.format("Invalid filter query: '%s'", facetField));
                }
            }
        return filters.get();
    }
}
