package nix.education.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import nix.education.entity.Book;
import nix.education.entity.ElasticBook;
import nix.education.repository.BookRepository;
import nix.education.service.BookDataService;
import nix.education.service.ElasticBookMapperService;
import nix.education.service.query.elasticsearch.ElasticsearchQueryBuilderService;
import nix.education.service.response.elasticsearch.ElasticsearchResponseService;
import nix.education.service.suggestions.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders.dateHistogram;

@Service
public class ElasticsearchService implements SearchService {

    @Autowired
    @Qualifier("elasticsearchSuggestionService")
    private SuggestionService suggestionService;

    @Autowired
    private ElasticsearchResponseService elasticsearchResponseService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private BookDataService bookDataService;

    @Autowired
    private ElasticBookMapperService bookMapperService;

    @Autowired
    private ElasticsearchQueryBuilderService elasticsearchQueryBuilderService;

    @Value("${elastic.index}")
    private String index;

    @Value("${search.elastic-range-facets}")
    private Set<String> rangeAggregations;

    @Value("${search.elastic-term-facets}")
    private Set<String> termAggregations;

    private static final int DOC_MIN_COUNT = 1;

    private static final String FIVE_YEARS_IN_DAYS = "1826d";

    @Override
    @SneakyThrows
    public Book addBook(Book book) {
        bookDataService.setTags(book);
        bookRepository.save(book);
        elasticsearchClient.index(i -> i.index(index)
            .id(String.valueOf(book.getId()))
            .document(bookMapperService.getElasticBook(book)));
        return book;
    }

    @Override
    @SneakyThrows
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        DeleteRequest deleteRequest = DeleteRequest
            .of(book -> book.index(index).id(String.valueOf(id)));
        elasticsearchClient.delete(deleteRequest);
    }

    @Override
    @SneakyThrows
    public List<Book> findAll() {
        return (List<Book>) bookRepository.findAll();
    }

    @Override
    @SneakyThrows
    public Map<String, Object> findBooks(String q, Optional<Set<String>> fq) {
        BoolQuery.Builder boolQuery = elasticsearchQueryBuilderService.getQuery(q, fq);
        SearchRequest request = new SearchRequest.Builder()
            .index(index).query(boolQuery.build()._toQuery())
            .aggregations(buildAggregations()).build();

        SearchResponse<ElasticBook> response =
            elasticsearchClient.search(request, ElasticBook.class);

        return elasticsearchResponseService.prepareResponse(response);
    }

    @Override
    public Map<String, Object> getAuthorSuggestions(String q) {
        return suggestionService.prepareAuthorSuggestionsResponse(q);
    }

    @Override
    public String initDataBase() {
        List<Book> elasticBooks = (List<Book>) bookRepository.findAll();
        elasticBooks.forEach(book -> {
            try {
                elasticsearchClient.index(i -> i.index(index)
                    .id(String.valueOf(book.getId()))
                    .document(bookMapperService.getElasticBook(book)));
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        });
        return String.format("Index \"%s\" was initialized successfully!", index);
    }

    private Map<String, Aggregation> buildAggregations() {
        Map<String, Aggregation> aggregations = new HashMap<>();

        termAggregations.forEach(field -> aggregations
            .put(field, AggregationBuilders.terms(f -> f.field(field))));
        rangeAggregations.forEach(field -> aggregations
            .put(field, dateHistogram(d -> d.field(field)
                .fixedInterval(Time.of(t -> t.time(FIVE_YEARS_IN_DAYS)))
                .minDocCount(DOC_MIN_COUNT))));
        return aggregations;
    }
}
