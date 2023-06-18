package nix.education.data;

import nix.education.entity.Tag;

import static nix.education.data.TestConstants.TEST_TAG;

public class TestTagData {

    private static Tag tag = setupTag();

    public static Tag getTag() {
        return tag;
    }

    private static Tag setupTag() {
        tag = new Tag();
        tag.setTag(TEST_TAG);
        tag.setId(1L);
        return tag;
    }
}
