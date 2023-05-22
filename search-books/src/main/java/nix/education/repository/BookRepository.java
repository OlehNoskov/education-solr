package nix.education.repository;

import java.util.List;
import nix.education.entity.Book;
import org.springframework.data.repository.CrudRepository;

public interface BookRepository extends CrudRepository<Book, Long> {

    List<Book> findBooksByIdIn(List<Long> ids);
}
