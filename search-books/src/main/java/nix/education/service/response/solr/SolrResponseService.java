package nix.education.service.response.solr;

import java.util.Map;
import org.apache.solr.client.solrj.response.QueryResponse;

public interface SolrResponseService {
    Map<String, Object> prepareResponse(QueryResponse response);
}
