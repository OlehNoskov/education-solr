package edu.nix.filters;

import com.google.common.base.Preconditions;
import edu.nix.util.HTTPStatus;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class ResolveUrlTokenFilter extends TokenFilter {
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private static final Pattern SHORT_URL_PATTERN = Pattern.compile("https://bit.ly/\\w+");
    private static final Set<Integer> RESPONSE_CODES =
        Set.of(HTTPStatus.MOVED_PERMANENTLY, HTTPStatus.MOVED_TEMPORARILY);
    private final HttpClient httpClient;

    protected ResolveUrlTokenFilter(TokenStream input, HttpClient httpClient) {
        super(input);
        this.httpClient = httpClient;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }
        char[] term = termAttribute.buffer();
        int len = termAttribute.length();
        String token = new String(term, 0, len);

        if (SHORT_URL_PATTERN.matcher(token).matches()) {
            termAttribute.setEmpty().append(getUrl(token));
        }
        return true;
    }

    private String getUrl(String url) throws IOException {
        HttpHead request = new HttpHead(url);
        HttpResponse httpResponse = httpClient.execute(request);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (!RESPONSE_CODES.contains(statusCode)) {
            return url;
        }

        Header[] headers = httpResponse.getHeaders(HttpHeaders.LOCATION);
        Preconditions.checkState(headers.length == 1);
        String resolvedUrl = headers[0].getValue();
        request.releaseConnection();
        return resolvedUrl;
    }
}
