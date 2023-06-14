package nix.education.service.response.elasticsearch;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import java.util.Map;
import nix.education.entity.ElasticBook;

public interface ElasticsearchResponseService {

    Map<String, Object> prepareResponse(SearchResponse<ElasticBook> response);
}
