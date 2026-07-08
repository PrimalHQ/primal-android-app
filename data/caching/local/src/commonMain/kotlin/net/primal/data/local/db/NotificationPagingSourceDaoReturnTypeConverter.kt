package net.primal.data.local.db

import androidx.paging.PagingSource
import androidx.room3.DaoReturnTypeConverter
import androidx.room3.OperationType
import androidx.room3.RoomDatabase
import androidx.room3.RoomRawQuery
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.notifications.NotificationData
import net.primal.data.local.dao.notifications.NotificationGroupCrossRef

/**
 * A [PagingSource] return-type converter for the notifications DAO that narrows the observed-table
 * set, the notifications counterpart of [FeedPagingSourceDaoReturnTypeConverter].
 *
 * Room 3 codegen observes the return POJO's `@Relation` tables on top of the query's tables — for
 * `NotificationDao.seenByGroupPaged` that is 9 app-wide tables (NotificationData,
 * NotificationGroupCrossRef, ProfileData, PostData, EventStats, EventUserStats, EventUri,
 * EventUriNostr, StreamData). Persisting any feed page or thread writes most of these, and a
 * ProfileDetails visit writes all of them, guaranteeing the notifications pager is invalidated
 * while off-screen and re-runs its full 7-relation query on back-navigation.
 *
 * The notifications list must re-query only when its rendered output can actually change live:
 *  - [NotificationData] — new notifications and seen-state updates,
 *  - [NotificationGroupCrossRef] — group membership (the paged query's join table),
 *  - [EventUserStats] — the user's own interaction flags (liked/replied/reposted/zapped) rendered
 *    on the notification's embedded note.
 *
 * Everything else the relations read is refreshed on the next structural invalidation instead of
 * live. Behavior is pinned by `NotificationPagingSourceInvalidationTest` in
 * `:data:caching:repository` desktopTest.
 */
class NotificationPagingSourceDaoReturnTypeConverter {

    private val delegate = PagingSourceDaoReturnTypeConverter()

    @Suppress("UnusedParameter")
    @DaoReturnTypeConverter(operations = [OperationType.READ])
    fun <T : Any> convert(
        database: RoomDatabase,
        tableNames: Array<String>,
        roomRawQuery: RoomRawQuery,
        executeAndConvert: suspend (RoomRawQuery) -> List<T>,
    ): PagingSource<Int, T> =
        delegate.convert(
            database = database,
            tableNames = NOTIFICATION_OBSERVED_TABLES,
            roomRawQuery = roomRawQuery,
            executeAndConvert = executeAndConvert,
        )

    companion object {
        private val NOTIFICATION_OBSERVED_TABLES =
            arrayOf(
                NotificationData::class,
                NotificationGroupCrossRef::class,
                EventUserStats::class,
            )
                .map { requireNotNull(it.simpleName) }
                .toTypedArray()
    }
}
