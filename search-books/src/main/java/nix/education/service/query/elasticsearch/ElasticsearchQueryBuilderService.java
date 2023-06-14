package nix.education.service.query.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import java.util.Optional;
import java.util.Set;

public interface ElasticsearchQueryBuilderService {

    BoolQuery.Builder getQuery(String q, Optional<Set<String>> fq);
}
