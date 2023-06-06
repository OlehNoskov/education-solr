package nix.education.service.suggestions;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nix.education.util.SearchConstants;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import static nix.education.util.ResponseMessages.SUGGESTIONS_NOT_FOUND_MESSAGE;
import static nix.education.util.SearchConstants.AUTHOR_SUGGESTER;
import static nix.education.util.SearchConstants.NUM_FOUND;
import static nix.education.util.SearchConstants.SUGGEST;
import static nix.education.util.SearchConstants.SUGGESTIONS;

@Service
@AllArgsConstructor
public class SolrSuggestionService implements SuggestionService {

    private final SolrClient solrClient;

    @SneakyThrows
    @Override
    public Map<String, Object> prepareAuthorSuggestionsResponse(String q) {
        QueryResponse response = solrClient.query(getSuggestionSolrQuery(q));
        if (response.getSuggesterResponse().getSuggestedTerms().get(AUTHOR_SUGGESTER).isEmpty()) {
            return SUGGESTIONS_NOT_FOUND_MESSAGE;
        }
        return Map.of(SUGGEST, getAuthorSuggester(response, getSuggestionSolrQuery(q)));
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
        result.put(NUM_FOUND,
            response.getSuggesterResponse().getSuggestedTerms().get(AUTHOR_SUGGESTER).size());
        result.put(SUGGESTIONS,
            response.getSuggesterResponse().getSuggestedTerms().get(AUTHOR_SUGGESTER));
        return result;
    }
}
