package nix.education.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nix.education.entity.Book;
import nix.education.entity.Tag;
import nix.education.repository.BookRepository;
import nix.education.repository.TagRepository;
import nix.education.request.BookRequest;
import org.springframework.stereotype.Service;

@Service
public class SavingBookService {

    public Book getSavedBook(BookRequest bookRequest,
                             BookRepository bookRepository,
                             TagRepository tagRepository) {
        Book book = new Book();
        book.setAuthor(bookRequest.getAuthor());
        book.setTitle(bookRequest.getTitle());
        book.setDescription(bookRequest.getDescription());
        book.setPublication_date(bookRequest.getPublication_date());
        book.setTags(getRequestBookTags(bookRequest.getTags(), tagRepository));
        bookRepository.save(book);
        book.setTitleTags(getListTitleTags(book));

        return book;
    }

    public List<String> getListTitleTags(Book book) {
        return book.getTags().stream().map(Tag::getTag).collect(Collectors.toList());
    }

    public List<Book> getListBookByIds(List<Long> ids, BookRepository bookRepository) {
        List<Book> books = new ArrayList<>();
        ids.forEach(id -> books.add(bookRepository.findById(id).orElseThrow()));
        return books;
    }

    public List<Tag> getRequestBookTags(List<String> tags, TagRepository tagRepository) {
        List<Tag> allTags = (List<Tag>) tagRepository.findAll();
        List<Tag> newTags = new ArrayList<>();
        List<Tag> savedTags = allTags.stream().filter(tag -> tags.stream()
                .anyMatch(tagRequest -> tagRequest.equals(tag.getTag())))
            .collect(Collectors.toList());

        tags.removeAll(allTags.stream().map(Tag::getTag).collect(Collectors.toList()));
        tags.forEach(tag -> newTags.add(new Tag(tag)));
        savedTags.addAll(newTags);
        return savedTags;
    }
}
