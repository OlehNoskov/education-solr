package nix.education.service.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nix.education.entity.Book;

public interface SearchService {

    Book addBook(Book book);
    void deleteBook(Long id);
    List<Book> findAll();
    Map<String, Object> findBooks(String q, Optional<Set<String>> fq);
    Map<String, Object> getAuthorSuggestions(String q);
    String initDataBase();
}
