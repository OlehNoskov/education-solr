package edu.nix.filters;

import java.util.Map;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.lucene.analysis.TokenFilterFactory;
import org.apache.lucene.analysis.TokenStream;

public class ResolveUrlTokenFilterFactory extends TokenFilterFactory {

    public ResolveUrlTokenFilterFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new ResolveUrlTokenFilter(tokenStream,
            HttpClientBuilder.create().disableRedirectHandling().build());
    }
}
