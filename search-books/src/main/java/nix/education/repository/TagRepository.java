package nix.education.repository;

import java.util.List;
import nix.education.entity.Tag;
import org.springframework.data.repository.CrudRepository;

public interface TagRepository extends CrudRepository<Tag, Long> {

    List<Tag> findTagsByTagIn (List<String> tags);
}
