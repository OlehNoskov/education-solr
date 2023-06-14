package nix.education.service.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import nix.education.entity.Book;
import nix.education.repository.BookRepository;
import nix.education.service.BookDataService;
import nix.education.service.query.solr.SolrQueryBuilderService;
import nix.education.service.response.solr.SolrResponseService;
import nix.education.service.suggestions.SuggestionService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SolrSearchService implements SearchService {

    @Autowired
    @Qualifier("solrSuggestionService")
    private SuggestionService suggestionService;

    @Autowired
    private SolrResponseService responseService;

    @Autowired
    private SolrClient solrClient;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookDataService bookDataService;

    @Autowired
    private SolrQueryBuilderService solrQueryBuilderService;

    @Value("${solr.core}")
    private String coreName;

    @Override
    @SneakyThrows
    public Book addBook(Book book) {
        bookDataService.setTags(book);
        bookRepository.save(book);
        solrClient.addBean(book);
        solrClient.commit();
        return book;
    }

    @Override
    @SneakyThrows
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        solrClient.deleteById(String.valueOf(id));
        solrClient.commit();
    }

    @Override
    @SneakyThrows
    public List<Book> findAll() {
        return (List<Book>) bookRepository.findAll();
    }

    @Override
    @SneakyThrows
    public Map<String, Object> findBooks(String q, Optional<Set<String>> fq) {
        SolrQuery query = solrQueryBuilderService.getQuery(q, fq);
        QueryResponse response = solrClient.query(query);
        return responseService.prepareResponse(response);
    }

    @Override
    public Map<String, Object> getAuthorSuggestions(String q) {
        return suggestionService.prepareAuthorSuggestionsResponse(q);
    }

    @Override
    @SneakyThrows
    public String initDataBase() {
        List<Book> books = (List<Book>) bookRepository.findAll();
        for (Book book : books) {
            book.setTagTitles(bookDataService.getTagTitles(book));
            solrClient.addBean(book);
        }
        solrClient.commit();
        return String.format("Core \"%s\" was initialized successfully!", coreName);
    }
}
