@file:Suppress("TooManyFunctions")

package net.primal.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.primal.android.articles.db.ArticleDao
import net.primal.android.articles.db.ArticleData
import net.primal.android.articles.db.ArticleFeedCrossRef
import net.primal.android.articles.db.ArticleFeedCrossRefDao
import net.primal.android.attachments.db.AttachmentDao
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.db.serialization.AttachmentTypeConverters
import net.primal.android.core.serialization.room.ListsTypeConverters
import net.primal.android.explore.db.TrendingTopic
import net.primal.android.explore.db.TrendingTopicDao
import net.primal.android.feeds.db.Feed
import net.primal.android.feeds.db.FeedDao
import net.primal.android.messages.db.DirectMessageDao
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.messages.db.MessageConversationDao
import net.primal.android.messages.db.MessageConversationData
import net.primal.android.nostr.db.EventHints
import net.primal.android.nostr.db.EventHintsDao
import net.primal.android.note.db.EventStats
import net.primal.android.note.db.EventStatsDao
import net.primal.android.note.db.EventUserStats
import net.primal.android.note.db.EventUserStatsDao
import net.primal.android.note.db.EventZap
import net.primal.android.note.db.EventZapDao
import net.primal.android.notes.db.FeedPostDao
import net.primal.android.notes.db.FeedPostDataCrossRef
import net.primal.android.notes.db.FeedPostDataCrossRefDao
import net.primal.android.notes.db.FeedPostRemoteKey
import net.primal.android.notes.db.FeedPostRemoteKeyDao
import net.primal.android.notes.db.PostDao
import net.primal.android.notes.db.PostData
import net.primal.android.notes.db.RepostDao
import net.primal.android.notes.db.RepostData
import net.primal.android.notifications.db.NotificationDao
import net.primal.android.notifications.db.NotificationData
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.db.ProfileDataDao
import net.primal.android.profile.db.ProfileInteraction
import net.primal.android.profile.db.ProfileInteractionDao
import net.primal.android.profile.db.ProfileStats
import net.primal.android.profile.db.ProfileStatsDao
import net.primal.android.settings.muted.db.MutedUserDao
import net.primal.android.settings.muted.db.MutedUserData
import net.primal.android.thread.db.ArticleCommentCrossRef
import net.primal.android.thread.db.NoteConversationCrossRef
import net.primal.android.thread.db.ThreadConversationDao
import net.primal.android.user.db.Relay
import net.primal.android.user.db.RelayDao
import net.primal.android.wallet.db.WalletTransactionDao
import net.primal.android.wallet.db.WalletTransactionData

@Database(
    entities = [
        PostData::class,
        ProfileData::class,
        RepostData::class,
        EventStats::class,
        EventZap::class,
        EventUserStats::class,
        NoteNostrUri::class,
        NoteAttachment::class,
        Feed::class,
        FeedPostDataCrossRef::class,
        FeedPostRemoteKey::class,
        NoteConversationCrossRef::class,
        ProfileStats::class,
        TrendingTopic::class,
        NotificationData::class,
        MutedUserData::class,
        DirectMessageData::class,
        MessageConversationData::class,
        WalletTransactionData::class,
        Relay::class,
        EventHints::class,
        ProfileInteraction::class,
        ArticleData::class,
        ArticleCommentCrossRef::class,
        ArticleFeedCrossRef::class,
    ],
    version = 39,
    exportSchema = true,
)
@TypeConverters(
    ListsTypeConverters::class,
    AttachmentTypeConverters::class,
)
abstract class PrimalDatabase : RoomDatabase() {

    abstract fun profiles(): ProfileDataDao

    abstract fun posts(): PostDao

    abstract fun reposts(): RepostDao

    abstract fun eventStats(): EventStatsDao

    abstract fun attachments(): AttachmentDao

    abstract fun feeds(): FeedDao

    abstract fun eventUserStats(): EventUserStatsDao

    abstract fun eventZaps(): EventZapDao

    abstract fun profileStats(): ProfileStatsDao

    abstract fun feedsConnections(): FeedPostDataCrossRefDao

    abstract fun feedPosts(): FeedPostDao

    abstract fun feedPostsRemoteKeys(): FeedPostRemoteKeyDao

    abstract fun threadConversations(): ThreadConversationDao

    abstract fun trendingTopics(): TrendingTopicDao

    abstract fun notifications(): NotificationDao

    abstract fun mutedUsers(): MutedUserDao

    abstract fun messages(): DirectMessageDao

    abstract fun messageConversations(): MessageConversationDao

    abstract fun walletTransactions(): WalletTransactionDao

    abstract fun relays(): RelayDao

    abstract fun eventHints(): EventHintsDao

    abstract fun profileInteractions(): ProfileInteractionDao

    abstract fun articles(): ArticleDao

    abstract fun articleFeedsConnections(): ArticleFeedCrossRefDao
}
