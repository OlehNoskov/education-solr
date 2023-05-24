package nix.education.service.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nix.education.repository.BookRepository;
import nix.education.service.SavingBookService;
import nix.education.util.FacetFields;
import nix.education.util.SymbolConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SolrBookResponseService {

    private final SavingBookService SAVING_BOOK_SERVICE;

    @SneakyThrows
    public Map<String, Object> prepareResponse(SolrQuery query,
                                               SolrClient solrClient,
                                               BookRepository bookRepository) {
        QueryResponse response = solrClient.query(query);
        Map<String, Object> responseBody = new LinkedHashMap<>();
        if (response.getResults().isEmpty()) {
            return Map.of("message", "Sorry, any documents weren't found!");
        }
        responseBody.put("numFound", getListBookIds(response).size());
        responseBody.put("docs",
            SAVING_BOOK_SERVICE.getListBookByIds(getListBookIds(response), bookRepository));
        responseBody.put("facet_counts", prepareFacets(response));
        return responseBody;
    }

    private List<Long> getListBookIds(QueryResponse response) {
        return response.getResults().stream()
            .map(doc -> Long.parseLong(doc.get("id").toString()))
            .collect(Collectors.toList());
    }

    private Map<String, Object> prepareFacets(QueryResponse response) {
        Map<String, Object> facets = new HashMap<>();
        response.getFacetFields().stream().map(e -> prepareFacets(e.getName(),
            e.getValues())).forEach(e -> facets.put(
            e.keySet().stream().findFirst().get(), e.values().stream().findFirst().get()));

        response.getFacetRanges().stream().map(e -> prepareRangeFacets(e.getName(), e.getCounts()))
            .forEach(e -> facets.put(e.keySet()
                .stream().findFirst().get().toString(), e.values().stream().findFirst().get()));
        return facets;
    }

    private Map<String, Object> prepareFacets(String name, List<FacetField.Count> buckets) {
        List<Object> bucketList = new ArrayList<>();
        for (FacetField.Count bucket : buckets) {
            Map<String, Object> values = new HashMap<>();
            String facetName = StringUtils.capitalize(bucket.getName());
            values.put(FacetFields.FACET_COUNT, bucket.getCount());
            values.put(FacetFields.FACET_LABEL, facetName);
            values.put(FacetFields.FQ, StringUtils.join(name, SymbolConstants.COLON, facetName));
            bucketList.add(values);
        }
        return Map.of(name, bucketList);
    }

    private Map<String, Object> prepareRangeFacets(String name,
                                                   List<RangeFacet.Count> buckets) {
        List<Object> bucketList = new ArrayList<>();
        for (RangeFacet.Count bucket : buckets) {
            Map<String, Object> values = new HashMap<>();
            values.put(FacetFields.FACET_LABEL, prepareDateLabel(bucket));
            values.put(FacetFields.FACET_COUNT, bucket.getCount());
            values.put(FacetFields.FQ, prepareRangeResponse(bucket));
            bucketList.add(values);
        }
        return Map.of(name, bucketList);
    }

    private String prepareDateLabel(RangeFacet.Count bucket) {
        String startYear = bucket.getValue().substring(0, 4);
        String endYear =
            String.valueOf(Integer.parseInt(startYear) + FacetFields.DATE_RANGE_FACET_GAP);
        return String.format("%s TO %s", startYear, endYear);
    }

    private String prepareRangeResponse(RangeFacet.Count bucket) {
        RangeFacet rangeFacet = bucket.getRangeFacet();
        String value = bucket.getValue();
        return String.format("%s:[%s TO %s%s]", rangeFacet.getName(), value, value,
            rangeFacet.getGap());
    }
}
