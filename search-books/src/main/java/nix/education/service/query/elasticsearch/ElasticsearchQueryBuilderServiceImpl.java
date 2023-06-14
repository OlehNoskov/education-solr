package nix.education.service.query.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nix.education.util.SearchConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchQueryBuilderServiceImpl implements ElasticsearchQueryBuilderService {

    @Value("${search.elastic-term-facets}")
    private Set<String> termAggregations;

    @Value("${search.elastic-range-facets}")
    private Set<String> rangeAggregations;

    @Value("#{${search.search-fields}}")
    private Map<String, Float> fields;

    @Override
    public BoolQuery.Builder getQuery(String q, Optional<Set<String>> fq) {
        BoolQuery.Builder elasticQuery = buildSearchQuery(fields, q);
        return fq.isPresent() ?
            elasticQuery.filter(buildFilterQueries(matchFilterQuery(fq))) : elasticQuery;
    }

    private Map<String, String> matchFilterQuery(Optional<Set<String>> filters) {
        Map<String, String> matchedFilters = new HashMap<>();
        for (String filter : filters.get()) {
            String facetField = StringUtils.substringBefore(filter, SearchConstants.COLON);
            if (!termAggregations.contains(facetField) && !rangeAggregations.contains(facetField)) {
                throw new IllegalArgumentException("Invalid filter field!");
            }
            String facetValue = StringUtils.substringAfter(filter, SearchConstants.COLON);
            matchedFilters.put(facetField, facetValue);
        }
        return matchedFilters;
    }

    private List<Query> buildFilterQueries(Map<String, String> filters) {
        List<Query> fqs = new ArrayList<>();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (rangeAggregations.contains(entry.getKey())) {
                Query rangeQuery = buildRangeQuery(entry.getKey(), entry.getValue())._toQuery();
                fqs.add(rangeQuery);
            } else {
                Query termQuery = QueryBuilders.term(f -> f
                    .field(entry.getKey())
                    .value(entry.getValue()));
                fqs.add(termQuery);
            }
        }
        return fqs;
    }

    private BoolQuery.Builder buildSearchQuery(Map<String, Float> fields, String query) {
        List<Query> matchQueries = new ArrayList<>();
        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        for (Map.Entry<String, Float> entry : fields.entrySet()) {
            Query q = QueryBuilders
                .matchPhrase()
                .slop(3)
                .field(entry.getKey())
                .query(query)
                .boost(entry.getValue())
                .build()._toQuery();
            matchQueries.add(q);
        }
        return boolQuery.should(matchQueries);
    }

    private RangeQuery buildRangeQuery(String key, String fromTo) {
        String from =
            StringUtils.substringBetween(fromTo, SearchConstants.FROM, SearchConstants.TO);
        String to = StringUtils.substringAfter(fromTo, SearchConstants.TO);
        return QueryBuilders.range()
            .field(key)
            .from(from)
            .to(to)
            .build();
    }
}
