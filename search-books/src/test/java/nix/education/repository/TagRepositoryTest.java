package nix.education.repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import nix.education.BaseTestClass;
import nix.education.entity.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static nix.education.data.TestTagData.getTag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TagRepositoryTest extends BaseTestClass {

    @MockBean
    private TagRepository tagRepositoryMock;

    @Nested
    @DisplayName("\"save\" method should ")
    public class SaveTagTest {

        @Test
        @DisplayName("save new tag")
        public void shouldSaveNewTag() {
            Tag expectedTag = getTag();
            when(tagRepositoryMock.save(expectedTag)).thenReturn(expectedTag);
            Tag tag = tagRepositoryMock.save(expectedTag);

            assertEquals(expectedTag, tag);
        }
    }

    @Nested
    @DisplayName("\"findById\" method should ")
    public class FindByIdTest {

        @Test
        @DisplayName("find tag by id")
        public void shouldFindTagById() {
            Tag expectedTag = getTag();
            when(tagRepositoryMock.findById(1L)).thenReturn(Optional.of(expectedTag));
            Tag tag = tagRepositoryMock.findById(1L).get();

            assertEquals(expectedTag, tag);
        }
    }

    @Nested
    @DisplayName("\"findAll\" method should ")
    public class FindAllTest {

        @Test
        @DisplayName("find all tags")
        public void shouldFindAllTags() {
            List<Tag> expectedTags = List.of(getTag());
            when(tagRepositoryMock.findAll()).thenReturn(expectedTags);
            List<Tag> tags = (List<Tag>) tagRepositoryMock.findAll();

            assertEquals(expectedTags, tags);
        }
    }

    @Nested
    @DisplayName("\"findTagsByTagIn\" method should ")
    public class FindTagsByTagInTest {

        @Test
        @DisplayName("find tags by list of tag titles")
        void shouldFindTagByTag() {
            List<String> tagTitles = List.of("Test tag");
            List<Tag> expectedTags = List.of(getTag());
            when(tagRepositoryMock.findTagsByTagIn(tagTitles)).thenReturn(expectedTags);
            List<Tag> tags = tagRepositoryMock.findTagsByTagIn(tagTitles);

            assertEquals(expectedTags, tags);
        }

        @Test
        @DisplayName("not find any tags")
        void shouldNotFindTagByTag() {
            List<String> tagTitles = List.of("Book tag");
            List<Tag> expectedTags = Collections.emptyList();
            when(tagRepositoryMock.findTagsByTagIn(tagTitles)).thenReturn(Collections.emptyList());
            List<Tag> tags = tagRepositoryMock.findTagsByTagIn(tagTitles);

            assertEquals(expectedTags, tags);
        }
    }

    @Nested
    @DisplayName("\"delete\" method should ")
    public class DeleteTagTest {

        @Test
        @DisplayName("delete tag")
        public void shouldDeleteTag() {
            Tag tag = getTag();
            doNothing().when(tagRepositoryMock).delete(tag);
            tagRepositoryMock.delete(tag);

            verify(tagRepositoryMock).delete(tag);
        }

        @Test
        @DisplayName("delete tag by id")
        public void shouldDeleteTagById() {
            Tag tag = getTag();
            doNothing().when(tagRepositoryMock).deleteById(tag.getId());
            tagRepositoryMock.deleteById(tag.getId());

            verify(tagRepositoryMock).deleteById(tag.getId());
        }
    }
}
