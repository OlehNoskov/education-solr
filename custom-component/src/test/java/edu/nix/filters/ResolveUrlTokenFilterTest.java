package edu.nix.filters;

import java.io.IOException;
import java.io.StringReader;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_MOVED_PERMANENTLY;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResolveUrlTokenFilterTest {

    private HttpClient httpClient = mock(HttpClient.class);
    private HttpResponse httpResponse = mock(HttpResponse.class);
    private StatusLine statusLine = mock(StatusLine.class);
    private Header header = mock(Header.class);
    private Analyzer analyzer;
    private static final String RESOLVED_URL = "test-website.com";

    @Before
    public void init() throws IOException {
        analyzer = CustomAnalyzer.builder()
            .withTokenizer("whitespace")
            .build();
    }

    @Test
    public void incrementToken_shouldResolveShortenedUrl() throws Exception {
        String url = "https://bit.ly/43EpLuV";
        TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(url));
        ResolveUrlTokenFilter filter = new ResolveUrlTokenFilter(tokenStream, httpClient);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();

        when(httpClient.execute(any())).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getHeaders(anyString())).thenReturn(new Header[]{header});
        when(statusLine.getStatusCode()).thenReturn(SC_MOVED_PERMANENTLY);
        when(header.getValue()).thenReturn(RESOLVED_URL);

        filter.incrementToken();

        assertEquals(RESOLVED_URL, attr.toString());
    }

    @Test
    public void incrementToken_shouldReturnOriginalTokenIfUrlIsIncorrect() throws Exception {
        String incorrectUrl = "google.com";
        TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(incorrectUrl));
        ResolveUrlTokenFilter filter = new ResolveUrlTokenFilter(tokenStream, httpClient);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();

        filter.incrementToken();

        assertEquals(incorrectUrl, attr.toString());
    }
}
