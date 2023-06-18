package nix.education.controller;

import com.fasterxml.jackson.databind.JsonNode;
import nix.education.BaseTestClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static nix.education.data.FileContentReader.readFileContent;
import static nix.education.util.SearchConstants.FQ;
import static nix.education.util.SearchConstants.Q;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class BookControllerTest extends BaseTestClass {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("\"getBooks\" method should ")
    public class GetBooksTest {

        @Test
        @DisplayName("return all books")
        public void shouldReturnAllBooks() throws Exception {
            JsonNode expectedBody = readFileContent("all-books.json");
            mockMvc.perform(get("/books")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(String.valueOf(expectedBody)));
        }
    }

    @Nested
    @DisplayName("\"initSearchDatabase\" method should ")
    public class InitSearchDataBaseTest {

        @Test
        @DisplayName("init search database")
        public void shouldInitDatabaseAndReturnMessage() throws Exception {
            mockMvc.perform(post("/init")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("Core \"books\" was initialized successfully!"));
        }
    }

    @Nested
    @DisplayName("\"findBooks\" method should ")
    public class FindBooksTest {

        @Test
        @DisplayName("find books by query without filters")
        public void shouldFindBooksByQuery() throws Exception {
            JsonNode expectedBody = readFileContent("programming-query.json");
            mockMvc.perform(get("/search").param(Q, "Programming"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(String.valueOf(expectedBody)));
        }

        @Test
        @DisplayName("find books by query with filters")
        public void shouldFindBooksByQueryWithFilters() throws Exception {
            JsonNode expectedBody = readFileContent("multiple-tags-query.json");
            mockMvc.perform(get("/search")
                    .param(Q, "war")
                    .param(FQ, "tags:Drama")
                    .param(FQ, "tags:Historic"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(String.valueOf(expectedBody)));
        }

        @Test
        @DisplayName("not find any books by query")
        public void shouldNotFindBooksBy() throws Exception {
            JsonNode expectedBody = readFileContent("not-found-books.json");
            mockMvc.perform(get("/search")
                    .param(Q, "test query"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(String.valueOf(expectedBody)));
        }
    }

    @Nested
    @DisplayName("\"findSuggestsByQuery\" method should ")
    public class FindSuggestsByQueryTest {

        @Test
        @DisplayName("find author suggests by query")
        public void shouldFindAuthorSuggests() throws Exception {
            JsonNode expectedBody = readFileContent("author-suggestion.json");
            mockMvc.perform(get("/suggest").param(Q, "robe"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedBody)));
        }

        @Test
        @DisplayName("not find any author suggests")
        public void shouldNotFindAuthorSuggests() throws Exception {
            JsonNode expectedBody = readFileContent("not-found-suggestions.json");
            mockMvc.perform(get("/suggest").param(Q, "test query"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedBody)));
        }
    }
}
