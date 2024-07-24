@file:Suppress("TooManyFunctions")

package net.primal.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.primal.android.articles.db.ArticleDao
import net.primal.android.articles.db.ArticleData
import net.primal.android.attachments.db.AttachmentDao
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.db.serialization.AttachmentTypeConverters
import net.primal.android.core.serialization.room.ListsTypeConverters
import net.primal.android.explore.db.TrendingHashtag
import net.primal.android.explore.db.TrendingHashtagDao
import net.primal.android.feed.db.Feed
import net.primal.android.feed.db.FeedDao
import net.primal.android.feed.db.FeedPostDao
import net.primal.android.feed.db.FeedPostDataCrossRef
import net.primal.android.feed.db.FeedPostDataCrossRefDao
import net.primal.android.feed.db.FeedPostRemoteKey
import net.primal.android.feed.db.FeedPostRemoteKeyDao
import net.primal.android.feed.db.PostDao
import net.primal.android.feed.db.PostData
import net.primal.android.feed.db.RepostDao
import net.primal.android.feed.db.RepostData
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
import net.primal.android.thread.notes.db.ThreadConversationCrossRef
import net.primal.android.thread.notes.db.ThreadConversationCrossRefDao
import net.primal.android.thread.notes.db.ThreadConversationDao
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
        ThreadConversationCrossRef::class,
        ProfileStats::class,
        TrendingHashtag::class,
        NotificationData::class,
        MutedUserData::class,
        DirectMessageData::class,
        MessageConversationData::class,
        WalletTransactionData::class,
        Relay::class,
        EventHints::class,
        ProfileInteraction::class,
        ArticleData::class,
    ],
    version = 31,
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

    abstract fun conversationConnections(): ThreadConversationCrossRefDao

    abstract fun threadConversations(): ThreadConversationDao

    abstract fun trendingHashtags(): TrendingHashtagDao

    abstract fun notifications(): NotificationDao

    abstract fun mutedUsers(): MutedUserDao

    abstract fun messages(): DirectMessageDao

    abstract fun messageConversations(): MessageConversationDao

    abstract fun walletTransactions(): WalletTransactionDao

    abstract fun relays(): RelayDao

    abstract fun eventHints(): EventHintsDao

    abstract fun profileInteractions(): ProfileInteractionDao

    abstract fun articles(): ArticleDao
}
