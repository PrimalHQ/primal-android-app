package net.primal.android.feeds.domain

import io.kotest.matchers.shouldBe
import org.junit.Test

class FeedExtensionsKtTest {

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
    fun isNotesFeedSpec_forProperFeedSpec_returnsTrue() {
        val specId = "{\"id\":\"advsearch\",\"query\":\"kind:1 mehmedalija pas:1\"}"
        val specText = "{\"id\":\"latest\",\"kind\":\"notes\"}"
        specId.isNotesFeedSpec() shouldBe true
        specText.isNotesFeedSpec() shouldBe true
    }

    @Test
    fun isReadsFeedSpec_forProperFeedSpec_returnsTrue() {
        val specId = "{\"id\":\"advsearch\",\"query\":\"kind:30023 code pas:1\"}"
        val specText = "{\"id\":\"nostr-reads-feed\",\"kind\":\"reads\"}"
        specId.isReadsFeedSpec() shouldBe true
        specText.isReadsFeedSpec() shouldBe true
    }

    @Test
    fun isImageFeedSpec_forProperFeedSpec_returnsTrue() {
        val spec = "{\"id\":\"advsearch\",\"query\":\"filter:image waterfall pas:1\"}"
        spec.isImageSpec() shouldBe true
    }

    @Test
    fun isVideoFeedSpec_forProperFeedSpec_returnsTrue() {
        val spec = "{\"id\":\"advsearch\",\"query\":\"filter:video running pas:1\"}"
        spec.isVideoSpec() shouldBe true
    }

    @Test
    fun isAudioFeedSpec_forProperFeedSpec_returnsTrue() {
        val spec = "{\"id\":\"advsearch\",\"query\":\"filter:audio music pas:1\"}"
        spec.isAudioSpec() shouldBe true
    }

//    @Test
//    fun isReadsBookmarkFeedSpec_forProperFeedSpec_returnsTrue() {
//        println(buildArticleBookmarksFeedSpec(userId = profileId))
//        val spec = "{\"id\":\"feed\",\"kind\":\"reads\"," +
//            "\"kinds\":[30023],\"notes\":\"bookmarks\",\"pubkey\":\"$profileId\"}"
//        println(spec)
//        spec.isReadsBookmarkFeedSpec() shouldBe true
//    }
//
//    @Test
//    fun isReadsBookmarkFeedSpec_forInvalidProfileIdInInFeedSpec_returnsFalse() {
//        val spec = "{\"id\":\"feed\",\"kind\":\"reads\"," +
//            "\"kinds\":[30023],\"notes\":\"bookmarks\",\"pubkey\":\"abc\"}"
//        spec.isReadsBookmarkFeedSpec() shouldBe false
//    }
}
