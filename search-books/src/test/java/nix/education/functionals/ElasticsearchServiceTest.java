package nix.education.functionals;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static nix.education.data.FileContentReader.MAPPER;
import static nix.education.data.FileContentReader.readFileContent;
import static nix.education.data.TestConstants.AUTHOR;
import static nix.education.data.TestConstants.DESCRIPTION;
import static nix.education.data.TestConstants.PUBLICATION_DATE;
import static nix.education.data.TestConstants.TAGS;
import static nix.education.data.TestConstants.TITLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElasticsearchServiceTest {
    private static final HttpClient httpClient = HttpClientBuilder.create().build();

    @Nested
    @DisplayName("\"findAll\" method should ")
    public class FindAllTest {

        @Test
        @DisplayName("find all books")
        public void shouldFindAllBooks() throws Exception {
            HttpGet request = new HttpGet("http://localhost:8080/books");
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
            JsonNode expectedBody = readFileContent("all-books.json");

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals(expectedBody.size(), responseBody.size());
            assertEquals(expectedBody, responseBody);
        }
    }

    @Nested
    @DisplayName("\"addBook\" method should ")
    public class AddBookTest {

        @Test
        @DisplayName("show bad request error")
        public void shouldShowBadRequestErrorWhenAddIncorrectNewBook() throws Exception {
            HttpPost request = new HttpPost("http://localhost:8080/add");
            HttpResponse response = httpClient.execute(request);

            assertEquals(HttpStatus.SC_BAD_REQUEST,
                response.getStatusLine().getStatusCode());
        }

        @Test
        @DisplayName("add new book")
        public void shouldAddNewBook() throws Exception {
            HttpPost request = new HttpPost("http://localhost:8080/add");
            request.addHeader("Content-Type", "application/json");
            String filePath = "src/test/resources/add-book-request.json";
            String requestEntity = new String(Files.readAllBytes(Paths.get(filePath)));
            StringEntity params = new StringEntity(requestEntity);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
            JsonNode expectedBody = readFileContent("add-book-response.json");

            HttpDelete requestDelete =
                new HttpDelete("http://localhost:8080/delete/" + responseBody.get("id"));
            httpClient.execute(requestDelete);

            assertEquals(expectedBody.get(AUTHOR), responseBody.get(AUTHOR));
            assertEquals(expectedBody.get(TITLE), responseBody.get(TITLE));
            assertEquals(expectedBody.get(DESCRIPTION), responseBody.get(DESCRIPTION));
            assertEquals(expectedBody.get(TAGS), responseBody.get(TAGS));
            assertEquals(expectedBody.get(PUBLICATION_DATE), responseBody.get(PUBLICATION_DATE));
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
        }
    }

    @Nested
    @DisplayName("\"deleteBook\" method should ")
    public class DeleteBookTest {

        @Test
        @DisplayName("delete book by id")
        public void shouldDeleteBookById() throws Exception {
            HttpPost request = new HttpPost("http://localhost:8080/add");
            request.addHeader("Content-Type", "application/json");
            String filePath = "src/test/resources/add-book-request.json";
            String requestEntity = new String(Files.readAllBytes(Paths.get(filePath)));
            StringEntity params = new StringEntity(requestEntity);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));

            HttpDelete requestDelete =
                new HttpDelete("http://localhost:8080/delete/" + responseBody.get("id"));
            HttpResponse deleteResponse = httpClient.execute(requestDelete);
            assertEquals(HttpStatus.SC_NO_CONTENT,
                deleteResponse.getStatusLine().getStatusCode());
        }

        @Test
        @DisplayName("show error message")
        public void shouldShowErrorMessage() throws Exception {
            HttpDelete request = new HttpDelete("http://localhost:8080/delete/100");
            HttpResponse response = httpClient.execute(request);

            assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                response.getStatusLine().getStatusCode());
        }
    }

    @Nested
    @DisplayName("\"findBooks\" method should ")
    public class FindBooksTest {

        @Test
        @DisplayName("find books by query: \"programming\"")
        public void shouldFindBooksByProgrammingQuery() throws Exception {
            HttpGet request = new HttpGet("http://localhost:8080/search?q=Programming");
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
            JsonNode expectedBody = readFileContent("elastic-programming-query.json");

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals(expectedBody, responseBody);
        }

        @Test
        @DisplayName("find books by query: \"war\" and filter query: \"Drama\" and \"Historic\"")
        public void shouldFindBooksByTagsFilterQuery() throws Exception {
            HttpGet request =
                new HttpGet("http://localhost:8080/search?fq=tags:Drama&fq=tags:Historic&q=war");
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
            JsonNode expectedBody = readFileContent("elastic-multiple-tags-query.json");

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals(expectedBody, responseBody);
        }

        @Test
        @DisplayName("find books by query: \"programming\" and filter query publication_date")
        public void shouldFindBooksByDateQuery() throws Exception {
            HttpGet request =
                new HttpGet("http://localhost:8080/" +
                    "search?q=programming&fq=publicationDate:from1572649200000to1575244799000");
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
            JsonNode expectedBody = readFileContent("elastic-publication-query.json");

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals(expectedBody, responseBody);
        }

        @Test
        @DisplayName("don't find any books")
        public void shouldNotFindAnyBooks() throws Exception {
            HttpGet request =
                new HttpGet("http://localhost:8080/search?q=test_query");
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
            JsonNode expectedBody = readFileContent("not-found-books.json");

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals(expectedBody, responseBody);
        }
    }

    @Nested
    @DisplayName("\"getAuthorSuggestions\" method should ")
    public class GetAuthorSuggestionsTest {

        @Test
        @DisplayName("find suggestions by query")
        public void shouldFindAuthorFullName() throws Exception {
            HttpGet request = new HttpGet("http://localhost:8080/suggest?q=robe");
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
            JsonNode expectedBody = readFileContent("elastic-author-suggestion.json");

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals(expectedBody, responseBody);
        }

        @Test
        @DisplayName("don't find any suggestions")
        public void shouldNotFindAnySuggestions() throws Exception {
            HttpGet request =
                new HttpGet("http://localhost:8080/suggest?q=test_query");
            HttpResponse response = httpClient.execute(request);

            JsonNode responseBody =
                MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
            JsonNode expectedBody = readFileContent("not-found-suggestions.json");

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals(expectedBody, responseBody);
        }
    }
}
