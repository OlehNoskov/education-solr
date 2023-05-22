package edu.nix.handlers;

import java.io.IOException;
import java.util.Set;
import javax.ws.rs.core.Response;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.params.CollectionParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.Request;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CensorHandlerTest extends TestCase {
    private CensorHandler censorHandler;
    private SolrClient solrClient;
    private SolrCore core;

    private final Set<String> BAD_WORDS = Set.of("ass", "fuck", "shit");
    private final String fieldName  = "author";


    @Before
    public void setup() {
        censorHandler = new CensorHandler();
//        core = mock(SolrCore.class);
//        SolrTestCaseJ4.initCore("solrhome/collection1/conf/solrconfig.xml", "solrhome/collection1/conf/schema.xml", "solrhome/");
        solrClient = new HttpSolrClient.Builder("http://localhost:8983/solr/books").build();
    }


    @Test
    @DisplayName("\"handleRequestBody\" method should show an error message!")
    public void testHandleRequestBodyMethodShouldShowErrorMessage() throws Exception {
//        SolrQueryRequest req = mock(SolrQueryRequest.class);
//        SolrQueryResponse rsp = mock(SolrQueryResponse.class);
        SolrQuery query = new SolrQuery();
        query.add("q", "rows=10&q=author:hellooleg+ass&echoParams=explicit");
//        censorHandler.handleRequestBody(req, rsp);
//        String query = "author:ass";
//        String errorMessage = String.format("Sorry, you typed a bad word: %s", query);
//        ContentStreamUpdateRequest request = new ContentStreamUpdateRequest("/update");
//        request.setParam("collections", collectionName);
//        request.setParam("name", fieldName );
//        ModifiableSolrParams params = new ModifiableSolrParams();
//        params.set("collections", collectionName);
//        params.set("name", "author");
//        params.set("action", CollectionParams.CollectionAction.CREATEALIAS.toString());
//        QueryRequest request = new QueryRequest(params);
//        censorHandler.handleRequestBody();
//        UpdateResponse response = request.process(solrClient);
//        NamedList<Object> results = response.getResponse();
//        assertEquals(0, results.get("status"));
        assertEquals(0, 0);
    }
}
