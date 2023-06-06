package nix.education.service.suggestions;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nix.education.util.SearchConstants;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolrSuggestionService implements SuggestionService {

    private final SolrClient solrClient;
    private static final String AUTHOR_SUGGESTER = "authorSuggester";

    @SneakyThrows
    @Override
    public Map<String, Object> prepareAuthorSuggestionsResponse(String q) {
        QueryResponse response = solrClient.query(getSuggestionSolrQuery(q));
        if (response.getSuggesterResponse().getSuggestedTerms().get(AUTHOR_SUGGESTER).isEmpty()) {
            return Map.of("message", "Sorry, any suggestions weren't found!");
        }
        return Map.of("suggest", getAuthorSuggester(response, getSuggestionSolrQuery(q)));
    }

    private SolrQuery getSuggestionSolrQuery(String q) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/suggest");
        query.setParam("suggest.q", q);
        query.setParam("suggest", SearchConstants.TRUE);
        query.setParam("suggest.build", SearchConstants.TRUE);
        query.setParam("suggest.dictionary", AUTHOR_SUGGESTER);
        return query;
    }

    private Map<String, Object> getAuthorSuggester(QueryResponse response, SolrQuery query) {
        return Map.of(AUTHOR_SUGGESTER, getResponseResultsByQuery(response, query));
    }

    private Map<String, Object> getResponseResultsByQuery(QueryResponse response, SolrQuery query) {
        String q = query.getParams("suggest.q")[0];
        return Map.of(q, getAuthorSuggestions(response));
    }

    private Map<String, Object> getAuthorSuggestions(QueryResponse response) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("numFound",
            response.getSuggesterResponse().getSuggestions().get(AUTHOR_SUGGESTER).size());
        result.put("suggestions", response.getSuggesterResponse().getSuggestedTerms().values());
        return result;
    }
}
