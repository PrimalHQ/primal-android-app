package net.primal.android.feeds.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class NoteFeedExtensionsKtTest {

    private val profileId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"

    @Test
    fun isUserFeedSpec_forProperUserFeedSpec_returnsTrue() {
        "{\"id\":\"latest\",\"kind\":\"notes\"}".isUserNotesFeedSpec() shouldBe true
    }

    @Test
    fun isUserFeedSpec_forInvalidFieldOrderInFedSpec_returnsFalse() {
        "\"kind\":\"notes\",{\"id\":\"latest\"}".isUserNotesFeedSpec() shouldBe false
    }

    @Test
    fun isUserLwrFeedSpec_forProperUserLwrFeedSpec_returnsTrue() {
        "{\"id\":\"latest\",\"include_replies\":true,\"kind\":\"notes\"}".isUserNotesLwrFeedSpec() shouldBe true
    }

    @Test
    fun isUserLwrFeedSpec_forMisplacedIncludeRepliesField_inUserLwrFeedSpec_returnsFalse() {
        "{\"include_replies\":\"true\",\"id\":\"latest\",\"kind\":\"notes\"}".isUserNotesLwrFeedSpec() shouldBe false
        "{\"id\":\"latest\",\"kind\":\"notes\",\"include_replies\":\"true\"}".isUserNotesLwrFeedSpec() shouldBe false
    }

    @Test
    fun isProfileFeedSpec_forProperProfileFeedSpec_returnsTrue() {
        val spec = "{\"id\":\"feed\",\"kind\":\"notes\",\"pubkey\":\"$profileId\"}"
        spec.isProfileNotesFeedSpec() shouldBe true
    }

    @Test
    fun isProfileFeedSpec_forInvalidProfileIdInProfileFeedSpec_returnsFalse() {
        val spec = "{\"id\":\"feed\",\"kind\":\"notes\",\"pubkey\":\"abc\"}"
        spec.isProfileNotesFeedSpec() shouldBe false
    }

    @Test
    fun isProfileAuthoredFeedSpec_forProperProfileAuthoredFeedSpec_returnsTrue() {
        val spec = "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"authored\",\"pubkey\":\"$profileId\"}"
        spec.isProfileAuthoredNotesFeedSpec() shouldBe true
    }

    @Test
    fun isProfileAuthoredFeedSpec_forInvalidProfileIdInProfileAuthoredFeedSpec_returnsFalse() {
        val spec = "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"authored\",\"pubkey\":\"abc\"}"
        spec.isProfileAuthoredNotesFeedSpec() shouldBe false
    }

    @Test
    fun isProfileAuthoredRepliesFeedSpec_forProperFeedSpec_returnsTrue() {
        val spec = "{\"id\":\"feed\",\"include_replies\":true,\"kind\":\"notes\"," +
            "\"notes\":\"authored\",\"pubkey\":\"$profileId\"}"
        spec.isProfileAuthoredNoteRepliesFeedSpec() shouldBe true
    }

    @Test
    fun isProfileAuthoredRepliesFeedSpec_forInvalidProfileIdInFeedSpec_returnsFalse() {
        val spec = "{\"id\":\"feed\",\"include_replies\":true,\"kind\":\"notes\"," +
            "\"notes\":\"authored\",\"pubkey\":\"abc\"}"
        spec.isProfileAuthoredNoteRepliesFeedSpec() shouldBe false
    }

    @Test
    fun isNotesBookmarkFeedSpec_forProperFeedSpec_returnsTrue() {
        val spec = "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"bookmarks\",\"pubkey\":\"$profileId\"}"
        spec.isNotesBookmarkFeedSpec() shouldBe true
    }

    @Test
    fun isNotesBookmarkFeedSpec_forInvalidProfileIdInInFeedSpec_returnsFalse() {
        val spec = "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"bookmarks\",\"pubkey\":\"abc\"}"
        spec.isNotesBookmarkFeedSpec() shouldBe false
    }

    @Test
    fun isReadsBookmarkFeedSpec_forProperFeedSpec_returnsTrue() {
        val spec = "{\"id\":\"feed\",\"kind\":\"reads\"," +
            "\"kinds\":[30023],\"notes\":\"bookmarks\"},\"pubkey\":\"$profileId\"}"
        spec.isReadsBookmarkFeedSpec() shouldBe true
    }

    @Test
    fun isReadsBookmarkFeedSpec_forInvalidProfileIdInInFeedSpec_returnsFalse() {
        val spec = "{\"id\":\"feed\",\"kind\":\"reads\"," +
            "\"kinds\":[30023],\"notes\":\"bookmarks\"},\"pubkey\":\"abc\"}"
        spec.isReadsBookmarkFeedSpec() shouldBe false
    }
}
