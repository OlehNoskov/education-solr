package nix.education.service.suggestions;

import java.util.Map;
import lombok.SneakyThrows;

public interface SuggestionService {

    @SneakyThrows
    Map<String, Object> prepareAuthorSuggestionsResponse(String q);
}
