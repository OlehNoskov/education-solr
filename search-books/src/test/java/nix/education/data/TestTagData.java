package nix.education.data;

import nix.education.entity.Tag;

import static nix.education.data.TestConstants.TEST_TAG;

public class TestTagData {

    public static Tag getTag() {
        Tag tag = new Tag();
        tag.setTag(TEST_TAG);
        tag.setId(1L);
        return tag;
    }
}
