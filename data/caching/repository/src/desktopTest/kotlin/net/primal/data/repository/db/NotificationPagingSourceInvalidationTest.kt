package net.primal.data.repository.db

import androidx.paging.PagingSource
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.notifications.Notification
import net.primal.data.local.dao.notifications.NotificationData
import net.primal.data.local.dao.notifications.NotificationGroupCrossRef
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.streams.StreamData
import net.primal.data.local.db.CachingDatabase
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.EventUriType
import net.primal.domain.notifications.NotificationType
import net.primal.domain.streams.StreamStatus
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Pins the notifications [PagingSource]'s *effective* observed-table set — the tables whose writes
 * invalidate the notifications list and force a re-run of its 7-relation query.
 *
 * With the stock converter the [Notification] POJO's `@Relation` tables (ProfileData, PostData,
 * EventStats, EventUserStats, EventUri, EventUriNostr, StreamData) are all observed, so persisting
 * any feed page or thread — and especially a ProfileDetails visit — invalidates the notifications
 * pager and causes a stutter on back-navigation. The notifications list must instead invalidate
 * ONLY on:
 *  - [NotificationData] — new notifications and seen-state updates,
 *  - [NotificationGroupCrossRef] — group membership (the paged query's join table),
 *  - [EventUserStats] — the user's own interaction flags rendered live on the embedded note,
 * and must NOT invalidate on writes to any other table its relations read.
 */
class NotificationPagingSourceInvalidationTest {

    @Test
    fun notificationPagingSource_invalidates_on_NotificationData_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.notifications().upsertAll(
                data = listOf(notificationData(notificationId = "notification-2")),
            )
            pagingSource.awaitInvalidation(reason = "NotificationData write (new notification / seen update)")
        }

    @Test
    fun notificationPagingSource_invalidates_on_NotificationGroupCrossRef_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.notificationGroupCrossRef().insertAll(
                refs = listOf(
                    NotificationGroupCrossRef(
                        notificationId = "notification-2",
                        ownerId = USER_ID,
                        groupKey = GROUP_KEY,
                    ),
                ),
            )
            pagingSource.awaitInvalidation(reason = "NotificationGroupCrossRef write (group membership change)")
        }

    @Test
    fun notificationPagingSource_invalidates_on_EventUserStats_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.eventUserStats().upsert(
                data = EventUserStats(eventId = NOTE_ID, userId = USER_ID, liked = true),
            )
            pagingSource.awaitInvalidation(reason = "EventUserStats write (own interaction flags)")
        }

    @Test
    fun notificationPagingSource_doesNotInvalidate_on_ProfileData_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.profiles().insertOrUpdateAll(
                data = listOf(profileData(ownerId = ACTION_USER_ID).copy(displayName = "updated")),
            )
            pagingSource.assertNoInvalidation(reason = "ProfileData write (profile refresh)")
        }

    @Test
    fun notificationPagingSource_doesNotInvalidate_on_PostData_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.posts().upsertAll(data = listOf(postData(postId = "unrelated-note", authorId = "other")))
            pagingSource.assertNoInvalidation(reason = "PostData write (e.g. opening a thread)")
        }

    @Test
    fun notificationPagingSource_doesNotInvalidate_on_EventStats_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.eventStats().upsert(data = EventStats(eventId = NOTE_ID, likes = 42))
            pagingSource.assertNoInvalidation(reason = "EventStats write (count update)")
        }

    @Test
    fun notificationPagingSource_doesNotInvalidate_on_EventUri_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.eventUris().upsertAllEventUris(
                data = listOf(
                    EventUri(eventId = "unrelated-note", url = "https://primal.net/img.jpg", type = EventUriType.Image),
                ),
            )
            pagingSource.assertNoInvalidation(reason = "EventUri write (media links of persisted notes)")
        }

    @Test
    fun notificationPagingSource_doesNotInvalidate_on_EventUriNostr_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.eventUris().upsertAllEventNostrUris(
                data = listOf(
                    EventUriNostr(eventId = "unrelated-note", uri = "nostr:note1abc", type = EventUriNostrType.Note),
                ),
            )
            pagingSource.assertNoInvalidation(reason = "EventUriNostr write (nostr refs of persisted notes)")
        }

    @Test
    fun notificationPagingSource_doesNotInvalidate_on_StreamData_write() =
        withNotificationPagingSource { database, pagingSource ->
            database.streams().upsertStreamData(data = listOf(streamData(aTag = "30311:host:stream")))
            pagingSource.assertNoInvalidation(reason = "StreamData write (live stream updates)")
        }

    // ---------------------------------------------------------------------------------------------
    // harness
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a fresh db seeded with a single notification, builds the production notifications
     * [PagingSource] via [CachingDatabase.notifications], performs the initial load (which arms
     * Room's invalidation observer), then runs [block].
     */
    private fun withNotificationPagingSource(
        block: suspend (CachingDatabase, PagingSource<Int, Notification>) -> Unit,
    ) = runBlocking {
        val databaseName = "primal_notification_invalidation_${counter++}.db"
        LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
        val database = LocalDatabaseFactory.createDatabase<CachingDatabase>(databaseName = databaseName)
        try {
            database.profiles().insertOrUpdateAll(data = listOf(profileData(ownerId = ACTION_USER_ID)))
            database.posts().upsertAll(data = listOf(postData(postId = NOTE_ID, authorId = ACTION_USER_ID)))
            database.notifications().upsertAll(
                data = listOf(notificationData(notificationId = NOTIFICATION_ID)),
            )
            database.notificationGroupCrossRef().insertAll(
                refs = listOf(
                    NotificationGroupCrossRef(
                        notificationId = NOTIFICATION_ID,
                        ownerId = USER_ID,
                        groupKey = GROUP_KEY,
                    ),
                ),
            )

            val pagingSource = database.notifications().seenByGroupPaged(ownerId = USER_ID, groupKey = GROUP_KEY)

            val initialLoad = pagingSource.load(
                PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
            )
            val page = initialLoad as? PagingSource.LoadResult.Page
                ?: fail("initial load failed: $initialLoad")
            assertTrue(page.data.isNotEmpty(), "expected the seeded notification in the initial page")

            block(database, pagingSource)
        } finally {
            database.close()
            LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
        }
    }

    private suspend fun PagingSource<Int, Notification>.awaitInvalidation(reason: String) {
        val invalidated = withTimeoutOrNull(INVALIDATION_TIMEOUT_MS) {
            while (!invalid) delay(POLL_INTERVAL_MS)
            true
        }
        assertTrue(
            actual = invalidated == true,
            message = "expected notifications PagingSource to invalidate on: $reason",
        )
    }

    private suspend fun PagingSource<Int, Notification>.assertNoInvalidation(reason: String) {
        delay(NO_INVALIDATION_GRACE_MS)
        assertFalse(
            actual = invalid,
            message = "notifications PagingSource must NOT invalidate on: $reason",
        )
    }

    private fun notificationData(notificationId: String) =
        NotificationData(
            notificationId = notificationId,
            ownerId = USER_ID,
            createdAt = 1_700_000_000L,
            type = NotificationType.YOUR_POST_WAS_LIKED,
            seenGloballyAt = 1_700_000_100L,
            actionUserId = ACTION_USER_ID,
            actionPostId = NOTE_ID,
        )

    private fun postData(postId: String, authorId: String) =
        PostData(
            postId = postId,
            authorId = authorId,
            createdAt = 1_700_000_000L,
            tags = emptyList(),
            content = "hello nostr",
            uris = emptyList(),
            hashtags = emptyList(),
            sig = "sig",
            raw = "{}",
        )

    private fun profileData(ownerId: String) =
        ProfileData(
            ownerId = ownerId,
            eventId = "metadata-$ownerId",
            createdAt = 1_700_000_000L,
            raw = "{}",
        )

    private fun streamData(aTag: String) =
        StreamData(
            aTag = aTag,
            eventId = "stream-event-id",
            eventAuthorId = ACTION_USER_ID,
            mainHostId = ACTION_USER_ID,
            dTag = "stream",
            title = null,
            summary = null,
            imageUrl = null,
            hashtags = emptyList(),
            streamingUrl = null,
            recordingUrl = null,
            startsAt = null,
            endsAt = null,
            status = StreamStatus.LIVE,
            currentParticipants = null,
            totalParticipants = null,
            raw = "{}",
            createdAt = 1_700_000_000L,
        )

    companion object {
        private const val USER_ID = "user-pubkey"
        private const val ACTION_USER_ID = "action-user-pubkey"
        private const val NOTE_ID = "note-1"
        private const val NOTIFICATION_ID = "notification-1"
        private const val GROUP_KEY = "ALL"

        private const val INVALIDATION_TIMEOUT_MS = 3_000L
        private const val NO_INVALIDATION_GRACE_MS = 800L
        private const val POLL_INTERVAL_MS = 25L

        private var counter = 0
    }
}
