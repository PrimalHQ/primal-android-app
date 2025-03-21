package net.primal.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteDriver
import kotlin.coroutines.CoroutineContext
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.bookmarks.PublicBookmarkDao
import net.primal.data.local.dao.events.EventRelayHints
import net.primal.data.local.dao.events.EventRelayHintsDao
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventStatsDao
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriDao
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventUserStatsDao
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.events.EventZapDao
import net.primal.data.local.dao.explore.TrendingTopic
import net.primal.data.local.dao.explore.TrendingTopicDao
import net.primal.data.local.dao.feeds.Feed
import net.primal.data.local.dao.feeds.FeedDao
import net.primal.data.local.dao.messages.DirectMessageDao
import net.primal.data.local.dao.messages.DirectMessageData
import net.primal.data.local.dao.messages.MessageConversationDao
import net.primal.data.local.dao.messages.MessageConversationData
import net.primal.data.local.dao.notes.FeedPostDao
import net.primal.data.local.dao.notes.FeedPostDataCrossRef
import net.primal.data.local.dao.notes.FeedPostDataCrossRefDao
import net.primal.data.local.dao.notes.FeedPostRemoteKey
import net.primal.data.local.dao.notes.FeedPostRemoteKeyDao
import net.primal.data.local.dao.notes.PostDao
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.notes.RepostDao
import net.primal.data.local.dao.notes.RepostData
import net.primal.data.local.dao.notifications.NotificationDao
import net.primal.data.local.dao.notifications.NotificationData
import net.primal.data.local.dao.profiles.MutedUserDao
import net.primal.data.local.dao.profiles.MutedUserData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.profiles.ProfileDataDao
import net.primal.data.local.dao.profiles.ProfileStats
import net.primal.data.local.dao.profiles.ProfileStatsDao
import net.primal.data.local.dao.reads.ArticleDao
import net.primal.data.local.dao.reads.ArticleData
import net.primal.data.local.dao.reads.ArticleFeedCrossRef
import net.primal.data.local.dao.reads.ArticleFeedCrossRefDao
import net.primal.data.local.dao.reads.HighlightDao
import net.primal.data.local.dao.reads.HighlightData
import net.primal.data.local.dao.threads.ArticleCommentCrossRef
import net.primal.data.local.dao.threads.NoteConversationCrossRef
import net.primal.data.local.dao.threads.ThreadConversationDao
import net.primal.data.local.serialization.CdnTypeConverters
import net.primal.data.local.serialization.JsonTypeConverters
import net.primal.data.local.serialization.ListsTypeConverters
import net.primal.data.local.serialization.NostrReferenceTypeConverters
import net.primal.data.local.serialization.ProfileTypeConverters

@Database(
    entities = [
        PostData::class,
        ProfileData::class,
        ProfileStats::class,
        RepostData::class,
        EventStats::class,
        EventZap::class,
        EventUserStats::class,
        EventUri::class,
        EventUriNostr::class,
        EventRelayHints::class,
        Feed::class,
        FeedPostDataCrossRef::class,
        FeedPostRemoteKey::class,
        NoteConversationCrossRef::class,
        TrendingTopic::class,
        NotificationData::class,
        MutedUserData::class,
        DirectMessageData::class,
        MessageConversationData::class,
        PublicBookmark::class,
        ArticleData::class,
        ArticleCommentCrossRef::class,
        ArticleFeedCrossRef::class,
        HighlightData::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(
    ListsTypeConverters::class,
    JsonTypeConverters::class,
    CdnTypeConverters::class,
    ProfileTypeConverters::class,
    NostrReferenceTypeConverters::class,
)
abstract class PrimalDatabase : RoomDatabase() {

    abstract fun profiles(): ProfileDataDao

    abstract fun profileStats(): ProfileStatsDao

    abstract fun posts(): PostDao

    abstract fun reposts(): RepostDao

    abstract fun feeds(): FeedDao

    abstract fun eventUserStats(): EventUserStatsDao

    abstract fun eventZaps(): EventZapDao

    abstract fun eventStats(): EventStatsDao

    abstract fun eventUris(): EventUriDao

    abstract fun eventHints(): EventRelayHintsDao

    abstract fun feedPosts(): FeedPostDao

    abstract fun feedsConnections(): FeedPostDataCrossRefDao

    abstract fun feedPostsRemoteKeys(): FeedPostRemoteKeyDao

    abstract fun threadConversations(): ThreadConversationDao

    abstract fun trendingTopics(): TrendingTopicDao

    abstract fun notifications(): NotificationDao

    abstract fun mutedUsers(): MutedUserDao

    abstract fun messages(): DirectMessageDao

    abstract fun messageConversations(): MessageConversationDao

    abstract fun publicBookmarks(): PublicBookmarkDao

    abstract fun articles(): ArticleDao

    abstract fun articleFeedsConnections(): ArticleFeedCrossRefDao

    abstract fun highlights(): HighlightDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<PrimalDatabase> {
    override fun initialize(): PrimalDatabase
}

internal fun buildPrimalDatabase(
    driver: SQLiteDriver,
    queryCoroutineContext: CoroutineContext,
    builder: RoomDatabase.Builder<PrimalDatabase>,
): PrimalDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setDriver(driver)
        .setQueryCoroutineContext(queryCoroutineContext)
        .build()
}
