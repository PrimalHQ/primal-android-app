@file:Suppress("TooManyFunctions")

package net.primal.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import net.primal.android.feed.db.FeedPostSync
import net.primal.android.feed.db.FeedPostSyncDao
import net.primal.android.feed.db.PostDao
import net.primal.android.feed.db.PostData
import net.primal.android.feed.db.PostStats
import net.primal.android.feed.db.PostStatsDao
import net.primal.android.feed.db.RepostDao
import net.primal.android.feed.db.RepostData
import net.primal.android.messages.db.DirectMessageDao
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.messages.db.MessageConversationDao
import net.primal.android.messages.db.MessageConversationData
import net.primal.android.notifications.db.NotificationDao
import net.primal.android.notifications.db.NotificationData
import net.primal.android.profile.db.PostUserStats
import net.primal.android.profile.db.PostUserStatsDao
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.db.ProfileDataDao
import net.primal.android.profile.db.ProfileStats
import net.primal.android.profile.db.ProfileStatsDao
import net.primal.android.settings.muted.db.MutedUserDao
import net.primal.android.settings.muted.db.MutedUserData
import net.primal.android.thread.db.ThreadConversationCrossRef
import net.primal.android.thread.db.ThreadConversationCrossRefDao
import net.primal.android.thread.db.ThreadConversationDao

@Database(
    entities = [
        PostData::class,
        ProfileData::class,
        RepostData::class,
        PostStats::class,
        NoteNostrUri::class,
        NoteAttachment::class,
        Feed::class,
        FeedPostDataCrossRef::class,
        FeedPostRemoteKey::class,
        FeedPostSync::class,
        ThreadConversationCrossRef::class,
        PostUserStats::class,
        ProfileStats::class,
        TrendingHashtag::class,
        NotificationData::class,
        MutedUserData::class,
        DirectMessageData::class,
        MessageConversationData::class,
    ],
    version = 12,
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

    abstract fun postStats(): PostStatsDao

    abstract fun attachments(): AttachmentDao

    abstract fun feeds(): FeedDao

    abstract fun feedsConnections(): FeedPostDataCrossRefDao

    abstract fun feedPosts(): FeedPostDao

    abstract fun feedPostsRemoteKeys(): FeedPostRemoteKeyDao

    abstract fun feedPostsSync(): FeedPostSyncDao

    abstract fun conversationConnections(): ThreadConversationCrossRefDao

    abstract fun threadConversations(): ThreadConversationDao

    abstract fun postUserStats(): PostUserStatsDao

    abstract fun trendingHashtags(): TrendingHashtagDao

    abstract fun profileStats(): ProfileStatsDao

    abstract fun notifications(): NotificationDao

    abstract fun mutedUsers(): MutedUserDao

    abstract fun messages(): DirectMessageDao

    abstract fun messageConversations(): MessageConversationDao
}
