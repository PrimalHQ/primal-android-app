package net.primal.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteDriver
import kotlin.coroutines.CoroutineContext
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
import net.primal.db.notes.FeedPostDao
import net.primal.db.notes.PostDao
import net.primal.db.notes.PostData
import net.primal.db.profiles.MutedUserDao
import net.primal.db.profiles.MutedUserData
import net.primal.db.profiles.ProfileData
import net.primal.db.profiles.ProfileDataDao
import net.primal.db.profiles.ProfileStats
import net.primal.db.profiles.ProfileStatsDao
import net.primal.db.profiles.serialization.ProfileTypeConverters
import net.primal.serialization.room.JsonTypeConverters
import net.primal.serialization.room.ListsTypeConverters

@Database(
    entities = [
        PostData::class,
        ProfileData::class,
        ProfileStats::class,
//        ProfileInteraction::class,
//        RepostData::class,
        EventStats::class,
        EventZap::class,
        EventUserStats::class,
        EventUri::class,
        EventUriNostr::class,
        EventRelayHints::class,
//        Feed::class,
//        FeedPostDataCrossRef::class,
//        FeedPostRemoteKey::class,
        NoteConversationCrossRef::class,
//        TrendingTopic::class,
//        NotificationData::class,
        MutedUserData::class,
//        DirectMessageData::class,
//        MessageConversationData::class,
//        WalletTransactionData::class,
//        Relay::class,
//        PublicBookmark::class,
//        ArticleData::class,
//        ArticleCommentCrossRef::class,
//        ArticleFeedCrossRef::class,
//        HighlightData::class,
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

//    abstract fun profileInteractions(): ProfileInteractionDao

    abstract fun posts(): PostDao

//    abstract fun reposts(): RepostDao

//    abstract fun feeds(): FeedDao

    abstract fun eventUserStats(): EventUserStatsDao

    abstract fun eventZaps(): EventZapDao

    abstract fun eventStats(): EventStatsDao

    abstract fun eventUris(): EventUriDao

    abstract fun eventHints(): EventRelayHintsDao

//    abstract fun feedsConnections(): FeedPostDataCrossRefDao

    abstract fun feedPosts(): FeedPostDao

//    abstract fun feedPostsRemoteKeys(): FeedPostRemoteKeyDao

    abstract fun threadConversations(): ThreadConversationDao

//    abstract fun trendingTopics(): TrendingTopicDao

//    abstract fun notifications(): NotificationDao

    abstract fun mutedUsers(): MutedUserDao

//    abstract fun messages(): DirectMessageDao

//    abstract fun messageConversations(): MessageConversationDao

//    abstract fun walletTransactions(): WalletTransactionDao

//    abstract fun relays(): RelayDao

//    abstract fun publicBookmarks(): PublicBookmarkDao

//    abstract fun articles(): ArticleDao

//    abstract fun articleFeedsConnections(): ArticleFeedCrossRefDao

//    abstract fun highlights(): HighlightDao
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
