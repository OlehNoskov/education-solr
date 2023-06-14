package nix.education.service;

import java.util.stream.Collectors;
import nix.education.entity.Book;
import nix.education.entity.ElasticBook;
import nix.education.entity.Tag;
import org.springframework.stereotype.Service;

@Service
public class ElasticBookMapperService {

    public ElasticBook getElasticBook(Book book) {
        ElasticBook elasticBook = new ElasticBook();
        elasticBook.setId(book.getId());
        elasticBook.setTitle(book.getTitle());
        elasticBook.setAuthor(book.getAuthor());
        elasticBook.setDescription(book.getDescription());
        if (book.getTagTitles() != null) {
            elasticBook.setTags(book.getTagTitles());
        } else {
            elasticBook.setTags(book.getTags().stream().map(Tag::getTag)
                .collect(Collectors.toList()));
        }
        elasticBook.setPublicationDate(book.getPublicationDate());
        return elasticBook;
    }
}
