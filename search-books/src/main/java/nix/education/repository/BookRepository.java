package nix.education.repository;

import org.springframework.data.repository.CrudRepository;
import nix.education.entity.Book;

public interface BookRepository extends CrudRepository<Book, Long> {
}
