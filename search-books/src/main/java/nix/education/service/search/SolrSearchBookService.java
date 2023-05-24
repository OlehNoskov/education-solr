package nix.education.service.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nix.education.entity.Book;
import nix.education.repository.BookRepository;
import nix.education.repository.TagRepository;
import nix.education.request.BookRequest;
import nix.education.service.SavingBookService;
import nix.education.service.query.SolrQueryBuilderService;
import nix.education.service.response.SolrBookResponseService;
import nix.education.service.suggestions.SolrBookAuthorSuggestionService;
import nix.education.util.SymbolConstants;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolrSearchBookService implements SearchService {

    private final SolrClient SOLR_CLIENT;
    private final BookRepository BOOK_REPOSITORY;
    private final TagRepository TAG_REPOSITORY;
    private final SavingBookService SAVING_BOOK_SERVICE;
    private final SolrBookResponseService SOLR_RESPONSE;
    private final SolrBookAuthorSuggestionService AUTHOR_SUGGESTION_SERVICE;
    private final SolrQueryBuilderService QUERY_BUILDER;

    @Value("${solr.collection}")
    private String coreName;

    @Override
    @SneakyThrows
    public Book addBook(BookRequest bookRequest) {
        Book savedBook =
            SAVING_BOOK_SERVICE.getSavedBook(bookRequest, BOOK_REPOSITORY, TAG_REPOSITORY);
        SOLR_CLIENT.addBean(savedBook);
        SOLR_CLIENT.commit();
        return savedBook;
    }

    @Override
    @SneakyThrows
    public void deleteBook(Long id) {
        BOOK_REPOSITORY.deleteById(id);
        SOLR_CLIENT.deleteById(id + SymbolConstants.EMPTY);
        SOLR_CLIENT.commit();
    }

    @Override
    @SneakyThrows
    public List<Book> findAll() {
        return (List<Book>) BOOK_REPOSITORY.findAll();
    }

    @Override
    public Map<String, Object> findBooks(String q, Optional<Set<String>> fq) {
        return SOLR_RESPONSE.prepareResponse(QUERY_BUILDER.getSolrQuery(q, fq), SOLR_CLIENT,
            BOOK_REPOSITORY);
    }

    @Override
    public Map<String, Object> getAuthorSuggestions(String q) {
        return AUTHOR_SUGGESTION_SERVICE.prepareAuthorSuggestionsResponse(q, SOLR_CLIENT);
    }

    @Override
    @SneakyThrows
    public String initDataBase() {
        List<Book> books = (List<Book>) BOOK_REPOSITORY.findAll();
        for (Book book : books) {
            book.setTitleTags(SAVING_BOOK_SERVICE.getListTitleTags(book));
            SOLR_CLIENT.addBean(book);
        }
        SOLR_CLIENT.commit();
        return "Core " + "\"" + coreName + "\"" + " was initialized successfully!";
    }
}
