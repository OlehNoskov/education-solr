package edu.nix.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

public class CensorHandler extends SearchHandler {
    private static final String BAD_WORDS_FILE = "bad-words.txt";
    private static final Set<String> BAD_WORDS = new HashSet<>();

    static {
        try (InputStream inputStream = CensorHandler.class.getClassLoader()
            .getResourceAsStream(BAD_WORDS_FILE)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String word;
                while ((word = reader.readLine()) != null) {
                    BAD_WORDS.add(word);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRequestBody(SolrQueryRequest request, SolrQueryResponse response)
        throws Exception {
        String query = request.getParams().get(CommonParams.Q);

        for (String badWord : BAD_WORDS) {
            if (StringUtils.containsIgnoreCase(query, badWord)) {
                response.addResponse(
                    "Sorry, you typed a bad word: " + query);
                return;
            }
        }
        super.handleRequestBody(request, response);
    }
}
