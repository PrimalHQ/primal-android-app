package net.primal.android.settings.repository

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import net.primal.android.nostr.model.primal.content.ContentFeedData
import net.primal.android.notes.db.OldFeed
import org.junit.Test

class OldFeedMapperTest {

    private val userId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"

    @Test
    fun toLatestWithRepliesDirective_createsProperLwrDirective() {
        val directive = userId.toLatestWithRepliesDirective()
        directive shouldBe "$LWR_DIRECTIVE_PREFIX;$userId"
    }

    @Test
    fun isLatestFeed_returnsTrueForLatestFeed() {
        OldFeed(name = "Latest", directive = userId).isLatest(userId) shouldBe true
    }

    @Test
    fun isLatestFeed_ignoresFeedNameAndReturnsTrue() {
        OldFeed(name = "XYZ", directive = userId).isLatest(userId) shouldBe true
    }

    @Test
    fun isLatestFeed_returnsFalseForLatestWithRepliesFeed() {
        OldFeed(name = "Latest", directive = "$LWR_DIRECTIVE_PREFIX;$userId").isLatest(userId) shouldBe false
    }

    @Test
    fun isLatestFeed_returnsFalseForOtherFeed() {
        OldFeed(name = "Latest", directive = "global;trending").isLatest(userId) shouldBe false
    }

    @Test
    fun isLatestWithReplies_returnsTrueForLatestWithRepliesFeed() {
        OldFeed(name = "LWR", directive = "$LWR_DIRECTIVE_PREFIX;$userId").isLatestWithReplies(userId) shouldBe true
    }

    @Test
    fun isLatestWithReplies_ignoresFeedNameAndReturnsTrue() {
        OldFeed(name = "XYZ", directive = "$LWR_DIRECTIVE_PREFIX;$userId").isLatestWithReplies(userId) shouldBe true
    }

    @Test
    fun isLatestWithReplies_returnsFalseForLatestFeed() {
        OldFeed(name = "LWR", directive = userId).isLatestWithReplies(userId) shouldBe false
    }

    @Test
    fun isLatestWithReplies_returnsFalseForOtherFeed() {
        OldFeed(name = "LWR", directive = "global;trending").isLatestWithReplies(userId) shouldBe false
    }

    @Test
    fun isLatestWithRepliesDirective_returnsTrueForLwrDirective() {
        "$LWR_DIRECTIVE_PREFIX;$userId".isLatestWithRepliesDirective() shouldBe true
    }

    @Test
    fun isLatestWithRepliesDirective_returnsFalseForLatestDirective() {
        userId.isLatestWithRepliesDirective() shouldBe false
    }

    @Test
    fun isLatestWithRepliesDirective_returnsFalseForInvalidUserId() {
        userId.substring(0, userId.length - 3).isLatestWithRepliesDirective() shouldBe false
    }

    @Test
    fun isLatestWithRepliesDirective_returnsFalseOtherDirective() {
        "global;trending".isLatestWithRepliesDirective() shouldBe false
    }

    @Test
    fun asFeedPO_latestFeedIsProperlyMapped() {
        val latestFeed = ContentFeedData(name = "Latest", directive = userId)
        val dbFeed = latestFeed.asFeedPO()
        dbFeed.shouldNotBeNull()
        dbFeed.name shouldBe "Latest"
        dbFeed.directive shouldBe userId
    }

    @Test
    fun asFeedPO_latestWithRepliesFeedIsProperlyMapped() {
        val lwrFeed = ContentFeedData(name = "LWR", directive = userId, includeReplies = true)
        val dbFeed = lwrFeed.asFeedPO()
        dbFeed.shouldNotBeNull()
        dbFeed.name shouldBe "LWR"
        dbFeed.directive shouldBe userId.toLatestWithRepliesDirective()
    }

    @Test
    fun asFeedPO_trendingFeedIsProperlyMapped() {
        val trendingFeed = ContentFeedData(name = "Trending 24h", directive = "global;trending")
        val dbFeed = trendingFeed.asFeedPO()
        dbFeed.shouldNotBeNull()
        dbFeed.name shouldBe "Trending 24h"
        dbFeed.directive shouldBe "global;trending"
    }

    @Test
    fun asContentFeedData_latestFeedIsProperlyMapped() {
        val latestFeed = OldFeed(name = "Latest", directive = userId)
        val settingsFeed = latestFeed.asContentFeedData()
        settingsFeed.name shouldBe "Latest"
        settingsFeed.directive shouldBe userId
        settingsFeed.includeReplies shouldBe null
    }

    @Test
    fun asContentFeedData_latestWithRepliesFeedIsProperlyMapped() {
        val lwrFeed = OldFeed(name = "LWR", directive = userId.toLatestWithRepliesDirective())
        val settingsFeed = lwrFeed.asContentFeedData()
        settingsFeed.name shouldBe "LWR"
        settingsFeed.directive shouldBe userId
        settingsFeed.includeReplies shouldBe true
    }

    @Test
    fun asContentFeedData_trendingFeedIsProperlyMapped() {
        val trendingFeed = OldFeed(name = "Trending 24h", directive = "global;trending")
        val settingsFeed = trendingFeed.asContentFeedData()
        settingsFeed.name shouldBe "Trending 24h"
        settingsFeed.directive shouldBe "global;trending"
        settingsFeed.includeReplies shouldBe null
    }
}
