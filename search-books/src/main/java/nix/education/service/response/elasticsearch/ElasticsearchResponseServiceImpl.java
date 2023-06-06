package nix.education.service.response.elasticsearch;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nix.education.entity.ElasticBook;
import nix.education.service.BookDataService;
import nix.education.util.SearchConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static nix.education.util.ResponseMessages.DOCUMENTS_NOT_FOUND_MESSAGE;

@Service
@AllArgsConstructor
public class ElasticsearchResponseServiceImpl implements ElasticsearchResponseService {

    private final BookDataService bookDataService;

    @SneakyThrows
    @Override
    public Map<String, Object> prepareResponse(SearchResponse<ElasticBook> response) {
        if (response.hits().hits().isEmpty()) {
            return DOCUMENTS_NOT_FOUND_MESSAGE;
        }
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(SearchConstants.NUM_FOUND, response.hits().hits().size());
        responseBody.put(SearchConstants.DOCS, bookDataService.getBooks(getIdsArray(response)));
        responseBody.put(SearchConstants.FACET_COUNTS, prepareAggregations(response));

        return responseBody;
    }

    private List<Long> getIdsArray(SearchResponse<ElasticBook> response) {
        return response.hits().hits().stream()
            .map(hit -> Long.parseLong(hit.id())).collect(Collectors.toList());
    }

    private Map<String, Object> prepareAggregations(SearchResponse<ElasticBook> response) {
        List<Map<String, Object>> aggregations = new ArrayList<>();
        response.aggregations()
            .forEach((key, value) -> {
                if (value.isSterms()) {
                    aggregations.add(prepareTermsAggregations(key, value));
                } else if (value.isDateHistogram()) {
                    aggregations.add(prepareRangeAggregations(key, value));
                }
            });
        Map<String, Object> result = new HashMap<>();
        aggregations.forEach(map -> map.keySet().forEach(key -> result.put(key, map.get(key))));
        return result;
    }

    private Map<String, Object> prepareTermsAggregations(String name, Aggregate buckets) {
        List<Object> buketList = new ArrayList<>();
        for (StringTermsBucket bucket : buckets.sterms().buckets().array()) {
            Map<String, Object> values = new HashMap<>();
            values.put(SearchConstants.FACET_COUNT, bucket.docCount());
            values.put(SearchConstants.FACET_LABEL, bucket.key()._get());
            values.put(SearchConstants.FQ,
                StringUtils.join(name, SearchConstants.COLON, bucket.key()._get()));
            buketList.add(values);
        }
        return Map.of(name, buketList);
    }

    private Map<String, Object> prepareRangeAggregations(String name, Aggregate buckets) {
        List<Object> bucketList = new ArrayList<>();
        for (DateHistogramBucket bucket : buckets.dateHistogram().buckets().array()) {
            Map<String, Object> values = new HashMap<>();
            String date = bucket.keyAsString();
            values.put(SearchConstants.FACET_COUNT, bucket.docCount());
            values.put(SearchConstants.FACET_LABEL, prepareDateLabel(date));
            values.put(SearchConstants.FQ, prepareRangeResponse(name, date));
            bucketList.add(values);
        }
        if (name.equals("publicationDate")) {
            name = "publication_date";
        }
        return Map.of(name, bucketList);
    }

    private String prepareDateLabel(String date) {
        String startYear = date.substring(0, 4);
        String endYear =
            String.valueOf(Integer.parseInt(startYear) + SearchConstants.DATE_RANGE_FACET_GAP);
        return String.format("%s TO %s", startYear, endYear);
    }

    private String prepareRangeResponse(String name, String date) {
        return String.format("%s:[%s TO %s+5YEAR]", name, date, date);
    }
}
