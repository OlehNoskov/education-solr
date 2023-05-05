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
    private static final Set<String> LIST_BAD_WORDS = new HashSet<>();

    static {
        InputStream inputStream = CensorHandler.class.getClassLoader().getResourceAsStream(BAD_WORDS_FILE);
        try {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String word;
                while ((word = reader.readLine()) != null) {
                    LIST_BAD_WORDS.add(word.toLowerCase());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRequestBody(SolrQueryRequest request, SolrQueryResponse response)
        throws Exception {
        String q = request.getParams().get(CommonParams.Q);
        String query = StringUtils.substringAfter(q, ":").toLowerCase();

        for (String badWord : LIST_BAD_WORDS) {
            if (badWord.contains(query)) {
                response.addResponse(
                    "Sorry, you typed a bad word: " + query);
                return;
            }
        }
        super.handleRequestBody(request, response);
    }
}
