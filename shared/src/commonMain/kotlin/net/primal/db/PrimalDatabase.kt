package net.primal.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteDriver
import kotlin.coroutines.CoroutineContext
import net.primal.db.bookmarks.PublicBookmark
import net.primal.db.bookmarks.PublicBookmarkDao
import net.primal.db.conversation.ArticleCommentCrossRef
import net.primal.db.conversation.NoteConversationCrossRef
import net.primal.db.conversation.ThreadConversationDao
import net.primal.db.events.EventRelayHints
import net.primal.db.events.EventRelayHintsDao
import net.primal.db.events.EventStats
import net.primal.db.events.EventStatsDao
import net.primal.db.events.EventUri
import net.primal.db.events.EventUriDao
import net.primal.db.events.EventUriNostr
import net.primal.db.events.EventUserStats
import net.primal.db.events.EventUserStatsDao
import net.primal.db.events.EventZap
import net.primal.db.events.EventZapDao
import net.primal.db.events.serialization.EventUriTypeConverters
import net.primal.db.explore.TrendingTopic
import net.primal.db.explore.TrendingTopicDao
import net.primal.db.feeds.Feed
import net.primal.db.feeds.FeedDao
import net.primal.db.messages.DirectMessageDao
import net.primal.db.messages.DirectMessageData
import net.primal.db.messages.MessageConversationDao
import net.primal.db.messages.MessageConversationData
import net.primal.db.notes.FeedPostDao
import net.primal.db.notes.FeedPostDataCrossRef
import net.primal.db.notes.FeedPostDataCrossRefDao
import net.primal.db.notes.FeedPostRemoteKey
import net.primal.db.notes.FeedPostRemoteKeyDao
import net.primal.db.notes.PostDao
import net.primal.db.notes.PostData
import net.primal.db.notes.RepostDao
import net.primal.db.notes.RepostData
import net.primal.db.notifications.NotificationDao
import net.primal.db.notifications.NotificationData
import net.primal.db.profiles.MutedUserDao
import net.primal.db.profiles.MutedUserData
import net.primal.db.profiles.ProfileData
import net.primal.db.profiles.ProfileDataDao
import net.primal.db.profiles.ProfileStats
import net.primal.db.profiles.ProfileStatsDao
import net.primal.db.profiles.serialization.ProfileTypeConverters
import net.primal.db.reads.ArticleDao
import net.primal.db.reads.ArticleData
import net.primal.db.reads.ArticleFeedCrossRef
import net.primal.db.reads.ArticleFeedCrossRefDao
import net.primal.db.reads.HighlightDao
import net.primal.db.reads.HighlightData
import net.primal.serialization.room.JsonTypeConverters
import net.primal.serialization.room.ListsTypeConverters

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
//        ProfileInteraction::class,
//        WalletTransactionData::class,
//        Relay::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(
    ListsTypeConverters::class,
    JsonTypeConverters::class,
    EventUriTypeConverters::class,
    ProfileTypeConverters::class,
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

//    abstract fun relays(): RelayDao

//    abstract fun profileInteractions(): ProfileInteractionDao

//    abstract fun walletTransactions(): WalletTransactionDao
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
