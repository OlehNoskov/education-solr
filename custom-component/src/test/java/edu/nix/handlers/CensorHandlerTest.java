package edu.nix.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CensorHandlerTest {

    private final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();
    private final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void handleRequestBody_ShouldFindBadWord() throws Exception {
        HttpGet request = new HttpGet("http://localhost:8983/solr/books/censor?q=author:ass");
        HttpResponse response = HTTP_CLIENT.execute(request);

        JsonNode responseBody =
            MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
        JsonNode expectedBody = readFileContent("censor-bad-word-response.json");

        assertEquals(expectedBody.get("response"), responseBody.get("response"));
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    @Test
    public void handleRequestBody_ShouldNotFindBadWord() throws Exception {
        HttpGet request = new HttpGet("http://localhost:8983/solr/books/censor?q=author:test");
        HttpResponse response = HTTP_CLIENT.execute(request);
        JsonNode responseBody =
            MAPPER.readTree(EntityUtils.toString(response.getEntity(), "UTF-8"));
        JsonNode expectedBody = readFileContent("censor-valid-word-response..json");

        assertEquals(expectedBody.get("responseHeader").get("params").get("q"),
            responseBody.get("responseHeader").get("params").get("q"));
        assertEquals(expectedBody.get("response").get("numFound"),
            responseBody.get("response").get("numFound"));
        assertEquals(expectedBody.get("response").get("numFoundExact"),
            responseBody.get("response").get("numFoundExact"));
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    private JsonNode readFileContent(String fileName) throws IOException {
        return MAPPER.readTree(CensorHandler.class.getClassLoader().getResourceAsStream(fileName));
    }
}
