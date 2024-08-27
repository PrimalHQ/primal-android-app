package net.primal.android.core.ext

import io.kotest.matchers.shouldBe
import net.primal.android.crypto.CryptoUtils
import org.junit.Test

class FeedExtensionsKtTest {

    @Test
    fun isExploreTrendingFeed_returnsTrueForTrendingFeed() {
        "global;trending".isExploreTrendingFeed() shouldBe true
        "tribe;trending".isExploreTrendingFeed() shouldBe true
        "network;trending".isExploreTrendingFeed() shouldBe true
        "follows;trending".isExploreTrendingFeed() shouldBe true
    }

    @Test
    fun isExploreTrendingFeed_returnsFalseForNonTrendingFeed() {
        "search;something".isExploreTrendingFeed() shouldBe false
    }

    @Test
    fun isExploreTrendingFeed_returnsFalseForSearchingTrending() {
        "search;trending".isExploreTrendingFeed() shouldBe false
    }

    @Test
    fun isExploreTrendingFeed_returnsFalseForJustTrending() {
        "trending".isExploreTrendingFeed() shouldBe false
    }

    @Test
    fun isExplorePopularFeed_returnsTrueForPopularFeed() {
        "global;popular".isExplorePopularFeed() shouldBe true
        "tribe;popular".isExplorePopularFeed() shouldBe true
        "network;popular".isExplorePopularFeed() shouldBe true
        "follows;popular".isExplorePopularFeed() shouldBe true
    }

    @Test
    fun isExplorePopularFeed_returnsFalseForNonPopularFeed() {
        "search;invalid".isExplorePopularFeed() shouldBe false
    }

    @Test
    fun isExplorePopularFeed_returnsFalseForSearchingPopular() {
        "search;popular".isExplorePopularFeed() shouldBe false
    }

    @Test
    fun isExplorePopularFeed_returnsFalseForJustPopular() {
        "popular".isExplorePopularFeed() shouldBe false
    }

    @Test
    fun isExploreMostZappedFeed_returnsTrueForMostZappedFeed() {
        "global;mostzapped".isExploreMostZappedFeed() shouldBe true
        "tribe;mostzapped".isExploreMostZappedFeed() shouldBe true
        "network;mostzapped".isExploreMostZappedFeed() shouldBe true
        "follows;mostzapped".isExploreMostZappedFeed() shouldBe true
    }

    @Test
    fun isExploreMostZappedFeed_returnsFalseForNonTMostZappedFeed() {
        "search;invalid".isExploreMostZappedFeed() shouldBe false
    }

    @Test
    fun isExploreMostZappedFeed_returnsFalseForSearchingMostZapped() {
        "search;mostzapped".isExploreMostZappedFeed() shouldBe false
    }

    @Test
    fun isExploreMostZappedFeed_returnsFalseForJustMostZapped() {
        "mostzapped".isExploreMostZappedFeed() shouldBe false
    }

    @Test
    fun isExploreLatestFeed_returnsTrueForLatestFeed() {
        "global;latest".isExploreLatestFeed() shouldBe true
        "tribe;latest".isExploreLatestFeed() shouldBe true
        "network;latest".isExploreLatestFeed() shouldBe true
        "follows;latest".isExploreLatestFeed() shouldBe true
    }

    @Test
    fun isExploreLatestFeed_returnsFalseForNonLatestFeed() {
        "search;invalid".isExploreLatestFeed() shouldBe false
    }

    @Test
    fun isExploreLatestFeed_returnsFalseForSearchingLatest() {
        "search;latest".isExploreLatestFeed() shouldBe false
    }

    @Test
    fun isExploreLatestFeed_returnsFalseForJustLatest() {
        "latest".isExploreLatestFeed() shouldBe false
    }

    @Test
    fun isSearchFeed_returnsTrueForSearchFeed() {
        "search;primal".isSearchFeed() shouldBe true
    }

    @Test
    fun isSearchFeed_returnsFalseForReversedInvalidSearchFeed() {
        "primal;search".isSearchFeed() shouldBe false
    }

    @Test
    fun isSearchFeed_returnsFalseForFeedWithoutSearchPrefix() {
        "search".isSearchFeed() shouldBe false
    }

    @Test
    fun isBookmarkFeed_returnsTrueForCorrectBookmarksFeed() {
        "bookmarks;userHex".isBookmarkFeed() shouldBe true
    }

    @Test
    fun isBookmarkFeed_returnsFalseForReversedInvalidBookmarksFeed() {
        "userHex;bookmarks".isBookmarkFeed() shouldBe false
    }

    @Test
    fun isBookmarkFeed_returnsFalseForFeedWithoutBookmarksPrefix() {
        "bookmarks".isBookmarkFeed() shouldBe false
    }

    @Test
    fun isUserFeed_returnsTrueForCorrectUserHex() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        keypair.pubKey.isUserFeed() shouldBe true
    }

    @Test
    fun isUserFeed_returnsFalseForInvalidUserHex() {
        "randomInvalidUserHex".isUserFeed() shouldBe false
    }

    @Test
    fun isUserLWRFeed_returnsTrueForCorrectUserHex() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        "withreplies;${keypair.pubKey}".isUserLwrFeed() shouldBe true
    }

    @Test
    fun isUserLWRFeed_returnsFalseForCorrectPrefixButWrongUserHex() {
        "withreplies;thisIsNotOK".isUserLwrFeed() shouldBe false
    }

    @Test
    fun isUserLWRFeed_returnsFalseForJustWithRepliesPrefix() {
        "withreplies;".isUserLwrFeed() shouldBe false
    }

    @Test
    fun isUserAuthoredFeed_returnsTrueForCorrectFeed() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        "authored;${keypair.pubKey}".isUserAuthoredFeed() shouldBe true
    }

    @Test
    fun isUserAuthoredFeed_returnsFalseForCorrectPrefixButWrongUserHex() {
        "authored;random".isUserAuthoredFeed() shouldBe false
    }

    @Test
    fun isUserAuthoredFeed_returnsFalseForPrefixOnly() {
        "authored;".isUserAuthoredFeed() shouldBe false
    }

    @Test
    fun isUserAuthoredLwrFeed_returnsTrueForCorrectFeed() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        "authoredreplies;${keypair.pubKey}".isUserAuthoredRepliesFeed() shouldBe true
    }

    @Test
    fun isUserAuthoredLwrFeed_returnsFalseForCorrectPrefixButWrongUserHex() {
        "authoredreplies;random".isUserAuthoredRepliesFeed() shouldBe false
    }

    @Test
    fun isUserAuthoredLwrFeed_returnsFalseForPrefixOnly() {
        "authoredreplies;".isUserAuthoredRepliesFeed() shouldBe false
    }

    @Test
    fun isChronologicalFeed_returnsTrueForUserLatestFeed() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        keypair.pubKey.isChronologicalFeed() shouldBe true
    }

    @Test
    fun isChronologicalFeed_returnsTrueForUserLatestLwrFeed() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        "withreplies;${keypair.pubKey}".isChronologicalFeed() shouldBe true
    }

    @Test
    fun isChronologicalFeed_returnsTrueForUserAuthoredFeed() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        "authored;${keypair.pubKey}".isChronologicalFeed() shouldBe true
    }

    @Test
    fun isChronologicalFeed_returnsTrueForUserAuthoredLwrFeed() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        "authoredreplies;${keypair.pubKey}".isChronologicalFeed() shouldBe true
    }

    @Test
    fun isChronologicalFeed_returnsFalseForTrendingFeed() {
        "global;trending".isChronologicalFeed() shouldBe false
        "tribe;trending".isChronologicalFeed() shouldBe false
        "network;trending".isChronologicalFeed() shouldBe false
        "follows;trending".isChronologicalFeed() shouldBe false
    }

    @Test
    fun isChronologicalFeed_returnsFalseForPopularFeed() {
        "global;popular".isChronologicalFeed() shouldBe false
        "tribe;popular".isChronologicalFeed() shouldBe false
        "network;popular".isChronologicalFeed() shouldBe false
        "follows;popular".isChronologicalFeed() shouldBe false
    }

    @Test
    fun isChronologicalFeed_returnsFalseForMostZappedFeed() {
        "global;mostzapped".isChronologicalFeed() shouldBe false
        "tribe;mostzapped".isChronologicalFeed() shouldBe false
        "network;mostzapped".isChronologicalFeed() shouldBe false
        "follows;mostzapped".isChronologicalFeed() shouldBe false
    }

    @Test
    fun isChronologicalFeed_returnsTrueForLatestFeeds() {
        "global;latest".isChronologicalFeed() shouldBe true
        "tribe;latest".isChronologicalFeed() shouldBe true
        "network;latest".isChronologicalFeed() shouldBe true
        "follows;latest".isChronologicalFeed() shouldBe true
    }

    @Test
    fun hasUpwardsPagination_returnsTrueForUserFeeds() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        keypair.pubKey.hasUpwardsPagination() shouldBe true
        "withreplies;${keypair.pubKey}".hasUpwardsPagination() shouldBe true
    }

    @Test
    fun hasUpwardsPagination_returnsTrueForAuthoredFeeds() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        "authored;${keypair.pubKey}".hasUpwardsPagination() shouldBe true
        "authoredreplies;${keypair.pubKey}".hasUpwardsPagination() shouldBe true
    }

    @Test
    fun hasUpwardsPagination_returnsFalseForInvalidFeeds() {
        "global;mostzapped".hasUpwardsPagination() shouldBe false
        "tribe;mostzapped".hasUpwardsPagination() shouldBe false
        "network;mostzapped".hasUpwardsPagination() shouldBe false
        "follows;mostzapped".hasUpwardsPagination() shouldBe false

        "global;trending".hasUpwardsPagination() shouldBe false
        "tribe;trending".hasUpwardsPagination() shouldBe false
        "network;trending".hasUpwardsPagination() shouldBe false
        "follows;trending".hasUpwardsPagination() shouldBe false

        "global;latest".hasUpwardsPagination() shouldBe false
        "tribe;latest".hasUpwardsPagination() shouldBe false
        "network;latest".hasUpwardsPagination() shouldBe false
        "follows;latest".hasUpwardsPagination() shouldBe false

        "global;popular".hasUpwardsPagination() shouldBe false
        "tribe;popular".hasUpwardsPagination() shouldBe false
        "network;popular".hasUpwardsPagination() shouldBe false
        "follows;popular".hasUpwardsPagination() shouldBe false
    }

    @Test
    fun hasReposts_returnsTrueForUserAndUserAuthoredFeeds() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        keypair.pubKey.hasReposts() shouldBe true
        "withreplies;${keypair.pubKey}".hasReposts() shouldBe true
        "authored;${keypair.pubKey}".hasReposts() shouldBe true
    }

    @Test
    fun hasReposts_returnsFalseForInvalidFeeds() {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        "authoredreplies;${keypair.pubKey}".hasReposts() shouldBe true

        "global;mostzapped".hasReposts() shouldBe false
        "tribe;mostzapped".hasReposts() shouldBe false
        "network;mostzapped".hasReposts() shouldBe false
        "follows;mostzapped".hasReposts() shouldBe false

        "global;trending".hasReposts() shouldBe false
        "tribe;trending".hasReposts() shouldBe false
        "network;trending".hasReposts() shouldBe false
        "follows;trending".hasReposts() shouldBe false

        "global;latest".hasReposts() shouldBe false
        "tribe;latest".hasReposts() shouldBe false
        "network;latest".hasReposts() shouldBe false
        "follows;latest".hasReposts() shouldBe false

        "global;popular".hasReposts() shouldBe false
        "tribe;popular".hasReposts() shouldBe false
        "network;popular".hasReposts() shouldBe false
        "follows;popular".hasReposts() shouldBe false
    }
}
