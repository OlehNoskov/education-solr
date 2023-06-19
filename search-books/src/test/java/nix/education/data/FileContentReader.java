package nix.education.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import nix.education.entity.Book;

public class FileContentReader {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    @SneakyThrows
    public static JsonNode readFileContent(String fileName) {
        return MAPPER.readTree(Book.class.getClassLoader().getResourceAsStream(fileName));
    }
}
