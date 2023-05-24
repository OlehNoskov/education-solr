package nix.education.service.suggestions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.SneakyThrows;
import nix.education.util.SymbolsSolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

@Service
public class SolrBookAuthorSuggestionService {
    private static final String AUTHOR_SUGGESTER = "authorSuggester";

    @SneakyThrows
    public Map<String, Object> prepareAuthorSuggestionsResponse(String q, SolrClient solrClient) {
        QueryResponse response = solrClient.query(getSuggestionSolrQuery(q));
        Map<String, Object> responseBody = new HashMap<>();

        if (response.getSuggesterResponse().getSuggestedTerms().get(AUTHOR_SUGGESTER).isEmpty()) {
            return Map.of("message", "Sorry, any suggestions weren't found!");
        }
        responseBody.put("suggest", getAuthorSuggester(response, getSuggestionSolrQuery(q)));
        return responseBody;
    }

    private SolrQuery getSuggestionSolrQuery(String q) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/suggest");
        query.setParam("suggest.q", q);
        query.setParam("suggest", SymbolsSolrQuery.TRUE);
        query.setParam("suggest.build", SymbolsSolrQuery.TRUE);
        query.setParam("suggest.dictionary", AUTHOR_SUGGESTER);
        return query;
    }

    private Map<String, Object> getAuthorSuggester(QueryResponse response, SolrQuery query) {
        Map<String, Object> result = new HashMap<>();
        result.put(AUTHOR_SUGGESTER, getResponseResultsByQuery(response, query));
        return result;
    }

    private Map<String, Object> getResponseResultsByQuery(QueryResponse response, SolrQuery query) {
        Map<String, Object> result = new HashMap<>();
        String q = query.getParams("suggest.q")[0];
        result.put(q, getAuthorSuggestions(response));
        return result;
    }

    private Map<String, Object> getAuthorSuggestions(QueryResponse response) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("numFound",
            response.getSuggesterResponse().getSuggestions().get(AUTHOR_SUGGESTER).size());
        result.put("suggestions", response.getSuggesterResponse().getSuggestedTerms().values());
        return result;
    }
}
