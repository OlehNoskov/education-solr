package nix.education.util;

import java.util.Map;

public interface ResponseMessages {

    Map<String, Object> DOCUMENTS_NOT_FOUND_MESSAGE =
        Map.of("message", "Sorry, any documents weren't found!");

    Map<String, Object> SUGGESTIONS_NOT_FOUND_MESSAGE =
        Map.of("message", "Sorry, any suggestions weren't found!");
}
