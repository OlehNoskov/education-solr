package nix.education.service.response.solr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nix.education.service.BookDataService;
import nix.education.util.SearchConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.springframework.stereotype.Service;

import static nix.education.util.ResponseMessages.DOCUMENTS_NOT_FOUND_MESSAGE;

@Service
@AllArgsConstructor
public class SolrResponseServiceImpl implements SolrResponseService {

    private final BookDataService bookDataService;

    @SneakyThrows
    @Override
    public Map<String, Object> prepareResponse(QueryResponse response) {
        if (response.getResults().isEmpty()) {
            return DOCUMENTS_NOT_FOUND_MESSAGE;
        }
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(SearchConstants.NUM_FOUND, response.getResults().size());
        responseBody.put(SearchConstants.DOCS,
            bookDataService.getBooks(getListBookByIds(response)));
        responseBody.put(SearchConstants.FACET_COUNTS, prepareFacets(response));
        return responseBody;
    }

    private List<Long> getListBookByIds(QueryResponse response) {
        return response.getResults().stream()
            .map(doc -> Long.parseLong(doc.get("id").toString()))
            .collect(Collectors.toList());
    }

    private Map<String, Object> prepareFacets(QueryResponse response) {
        Map<String, Object> facets = new LinkedHashMap<>();
        response.getFacetRanges().stream().map(e -> prepareRangeFacets(e.getName(),
            e.getCounts())).forEach(e -> facets.put(e.keySet()
            .stream().findFirst().get().toString(), e.values().stream().findFirst().get()));

        response.getFacetFields().stream()
            .map(e -> prepareFacets(e.getName(), e.getValues()))
            .forEach(e -> facets.put(e.keySet().stream().findFirst().get(),
                e.values().stream().findFirst().get()));
        return facets;
    }

    private Map<String, Object> prepareFacets(String name, List<FacetField.Count> buckets) {
        if (name.equals("tags_str")) {
            name = "tags";
        }
        List<Object> bucketList = new ArrayList<>();
        for (FacetField.Count bucket : buckets) {
            Map<String, Object> values = new HashMap<>();
            String facetName = StringUtils.capitalize(bucket.getName());
            values.put(SearchConstants.FACET_COUNT, bucket.getCount());
            values.put(SearchConstants.FACET_LABEL, facetName);
            values.put(SearchConstants.FQ,
                StringUtils.join(name, SearchConstants.COLON, facetName));
            bucketList.add(values);
        }
        return Map.of(name, bucketList);
    }

    private Map<String, Object> prepareRangeFacets(String name,
                                                   List<RangeFacet.Count> buckets) {
        List<Object> bucketList = new ArrayList<>();
        for (RangeFacet.Count bucket : buckets) {
            Map<String, Object> values = new HashMap<>();
            values.put(SearchConstants.FACET_LABEL, prepareDateLabel(bucket));
            values.put(SearchConstants.FACET_COUNT, bucket.getCount());
            values.put(SearchConstants.FQ, prepareRangeResponse(bucket));
            bucketList.add(values);
        }
        return Map.of(name, bucketList);
    }

    private String prepareDateLabel(RangeFacet.Count bucket) {
        String startYear = bucket.getValue().substring(0, 4);
        String endYear =
            String.valueOf(Integer.parseInt(startYear) + SearchConstants.DATE_RANGE_FACET_GAP);
        return String.format("%s TO %s", startYear, endYear);
    }

    private String prepareRangeResponse(RangeFacet.Count bucket) {
        RangeFacet rangeFacet = bucket.getRangeFacet();
        String value = bucket.getValue();
        return String.format("%s:[%s TO %s%s]", rangeFacet.getName(), value, value,
            rangeFacet.getGap());
    }
}
