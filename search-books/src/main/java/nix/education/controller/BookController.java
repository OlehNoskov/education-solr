package nix.education.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nix.education.entity.Book;
import nix.education.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

    @Autowired
    @Qualifier("solrSearchService")
    private SearchService searchService;

    @GetMapping("/books")
    public List<Book> getBooks() {
        return searchService.findAll();
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Book addBook(@RequestBody Book book) {
        return searchService.addBook(book);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        searchService.deleteBook(id);
    }

    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> findBooks(@RequestParam(name = "q") String q,
                                         @RequestParam(name = "fq", required = false)
                                         Optional<Set<String>> fq) {
        return searchService.findBooks(q, fq);
    }

    @GetMapping("/suggest")
    @ResponseBody
    public Map<String, Object> findSuggestsByQuery(@RequestParam(name = "q") String q) {
        return searchService.getAuthorSuggestions(q);
    }

    @PostMapping("/init")
    public String initSearchDatabase() {
        return searchService.initDataBase();
    }
}
