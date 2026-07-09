package net.primal.data.repository.feed.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins [FeedSpecInvalidationTracker]'s per-spec routing: invalidating one `(ownerId, feedSpec)`
 * must invalidate only the sources tracked under that key. This is the contract the note feeds
 * rely on after `FeedPostDataCrossRef` was removed from the Room-observed table set — a feed's
 * writes must never regenerate another feed's PagingSource.
 */
class FeedSpecInvalidationTrackerTest {

    @Test
    fun invalidate_invalidatesTrackedSourceForSameSpec() {
        val tracker = FeedSpecInvalidationTracker()
        val source = tracker.track(OWNER_ID, FEED_SPEC_A, FakePagingSource())

        tracker.invalidate(ownerId = OWNER_ID, feedSpec = FEED_SPEC_A)

        assertTrue(source.invalid, "source tracked under the invalidated spec must invalidate")
    }

    @Test
    fun invalidate_doesNotInvalidateSourceOfOtherSpec() {
        val tracker = FeedSpecInvalidationTracker()
        val sourceA = tracker.track(OWNER_ID, FEED_SPEC_A, FakePagingSource())
        val sourceB = tracker.track(OWNER_ID, FEED_SPEC_B, FakePagingSource())

        tracker.invalidate(ownerId = OWNER_ID, feedSpec = FEED_SPEC_B)

        assertFalse(sourceA.invalid, "source of an unrelated spec must NOT invalidate")
        assertTrue(sourceB.invalid)
    }

    @Test
    fun invalidate_doesNotInvalidateSameSpecOfOtherOwner() {
        val tracker = FeedSpecInvalidationTracker()
        val source = tracker.track(OWNER_ID, FEED_SPEC_A, FakePagingSource())

        tracker.invalidate(ownerId = "other-owner", feedSpec = FEED_SPEC_A)

        assertFalse(source.invalid, "same spec under another owner must NOT invalidate")
    }

    @Test
    fun invalidate_invalidatesAllSourcesTrackedUnderSameSpec() {
        val tracker = FeedSpecInvalidationTracker()
        val first = tracker.track(OWNER_ID, FEED_SPEC_A, FakePagingSource())
        val second = tracker.track(OWNER_ID, FEED_SPEC_A, FakePagingSource())

        tracker.invalidate(ownerId = OWNER_ID, feedSpec = FEED_SPEC_A)

        assertTrue(first.invalid)
        assertTrue(second.invalid)
    }

    @Test
    fun invalidateAll_invalidatesEveryTrackedSource() {
        val tracker = FeedSpecInvalidationTracker()
        val sourceA = tracker.track(OWNER_ID, FEED_SPEC_A, FakePagingSource())
        val sourceB = tracker.track("other-owner", FEED_SPEC_B, FakePagingSource())

        tracker.invalidateAll()

        assertTrue(sourceA.invalid)
        assertTrue(sourceB.invalid)
    }

    private class FakePagingSource : PagingSource<Int, Int>() {
        override fun getRefreshKey(state: PagingState<Int, Int>): Int? = null
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Int> =
            LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
    }

    companion object {
        private const val OWNER_ID = "owner-pubkey"
        private const val FEED_SPEC_A = "feed-spec-a"
        private const val FEED_SPEC_B = "feed-spec-b"
    }
}
