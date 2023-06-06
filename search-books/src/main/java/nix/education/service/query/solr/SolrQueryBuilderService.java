package nix.education.service.query.solr;

import java.util.Optional;
import java.util.Set;
import org.apache.solr.client.solrj.SolrQuery;

public interface SolrQueryBuilderService {

    SolrQuery getQuery(String q, Optional<Set<String>> fq);
}
