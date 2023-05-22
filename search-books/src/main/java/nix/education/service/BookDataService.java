package nix.education.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import nix.education.entity.Book;
import nix.education.entity.Tag;
import nix.education.repository.BookRepository;
import nix.education.repository.TagRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookDataService {

    private final TagRepository tagRepository;
    private final BookRepository bookRepository;

    public void setTags(Book book) {
        List<Tag> bookTags = book.getTags();
        List<String> tagTitles = getTagTitles(book);
        List<Tag> existingTags = tagRepository.findTagsByTagIn(tagTitles);
        existingTags.forEach(
            existedTag -> bookTags.removeIf(tag -> tag.getTag().equals(existedTag.getTag())));

        List<Tag> resolvedTags = new ArrayList<>();
        resolvedTags.addAll(existingTags);
        resolvedTags.addAll(bookTags);

        book.setTags(resolvedTags);
        book.setTagTitles(tagTitles);
    }

    public List<String> getTagTitles(Book book) {
        return book.getTags().stream().map(Tag::getTag).collect(Collectors.toList());
    }

    public List<Book> getBooks(List<Long> ids) {
        List<Book> books = bookRepository.findBooksByIdIn(ids);
        books.sort(Comparator.comparing(book -> ids.indexOf(book.getId())));
        return books;
    }
}
