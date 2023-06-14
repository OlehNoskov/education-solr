package nix.education.service.suggestions;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.TermSuggestOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nix.education.entity.ElasticBook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static nix.education.util.ResponseMessages.SUGGESTIONS_NOT_FOUND_MESSAGE;
import static nix.education.util.SearchConstants.AUTHOR_SUGGESTER;
import static nix.education.util.SearchConstants.NUM_FOUND;
import static nix.education.util.SearchConstants.SUGGEST;
import static nix.education.util.SearchConstants.SUGGESTIONS;

@Service
@RequiredArgsConstructor
public class ElasticsearchSuggestionService implements SuggestionService {

    private final ElasticsearchClient elasticsearchClient;
    private static final String AUTHOR_FIELD = "author";

    @Value("${elastic.index}")
    private String index;

    @Override
    @SneakyThrows
    public Map<String, Object> prepareAuthorSuggestionsResponse(String query) {
        Map<String, FieldSuggester> suggesters = new HashMap<>();
        suggesters.put(AUTHOR_SUGGESTER,
            FieldSuggester.of(fs -> fs.term(f -> f.field(AUTHOR_FIELD))));
        Suggester suggester = Suggester.of(s -> s.suggesters(suggesters).text(query));

        SearchRequest request = new SearchRequest.Builder().index(index).suggest(suggester).build();
        SearchResponse<ElasticBook> response =
            elasticsearchClient.search(request, ElasticBook.class);

        if (response.suggest().get(AUTHOR_SUGGESTER).get(0).term().options().isEmpty()) {
            return SUGGESTIONS_NOT_FOUND_MESSAGE;
        }
        return Map.of(SUGGEST, getAuthorSuggestion(response, query));
    }

    private Map<String, Object> getAuthorSuggestion(SearchResponse<ElasticBook> response,
                                                    String query) {
        return Map.of(AUTHOR_SUGGESTER, getAuthorSuggestionResults(response, query));
    }

    private Map<String, Object> getAuthorSuggestionResults(SearchResponse<ElasticBook> response,
                                                           String query) {
        return Map.of(query, getAuthorSuggestions(response));
    }

    private Map<String, Object> getAuthorSuggestions(SearchResponse<ElasticBook> response) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> suggestions = response.suggest().get(AUTHOR_SUGGESTER).get(0).
            term().options().stream().map(TermSuggestOption::text).collect(Collectors.toList());
        result.put(NUM_FOUND,
            response.suggest().get(AUTHOR_SUGGESTER).get(0).term().options().size());
        result.put(SUGGESTIONS, suggestions);
        return result;
    }
}
