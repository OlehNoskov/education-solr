package edu.nix;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class IndexingLambda {
    private static final String SOLR_EC2_URL = System.getenv("SOLR_EC2_URL");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String handleRequest(S3Event input) {
        String bucketName = input.getRecords().get(0).getS3().getBucket().getName();
        String key = input.getRecords().get(0).getS3().getObject().getKey();
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard().build();

        String document;
        try {
            document = getS3Object(amazonS3.getObject(new GetObjectRequest(bucketName, key)));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        indexDocument(document);

        return "Document has been indexed successfully!";
    }

    private void indexDocument(String document) {
        List<SolrInputDocument> solrInputDocument = jsonDocToSolrDoc(document);
        SolrClient solrClient = new HttpSolrClient.Builder(SOLR_EC2_URL).build();
        try {
            solrClient.add(solrInputDocument);
            solrClient.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getS3Object(S3Object s3Object) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(s3Object.getObjectContent().getDelegateStream()));
        StringBuilder jsonDocument = new StringBuilder();
        String line = reader.readLine();

        while (line != null) {
            jsonDocument.append(line);
            line = reader.readLine();
        }
        return jsonDocument.toString();
    }

    private List<SolrInputDocument> jsonDocToSolrDoc(String jsonDoc) {
        List<Map<String, String>> documents;
        List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
        TypeReference<List<Map<String, String>>> typeReference = new TypeReference<>() {};

        try {
            documents = OBJECT_MAPPER.readValue(jsonDoc, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        for (Map<String, String> document : documents) {
            SolrInputDocument solrInputDocument = new SolrInputDocument();
            document.keySet().forEach(field -> solrInputDocument.addField(field, document.get(field)));
            solrInputDocuments.add(solrInputDocument);
        }
        return solrInputDocuments;
    }
}
