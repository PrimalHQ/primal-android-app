package net.primal.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import net.primal.android.feed.db.MediaResource
import net.primal.android.feed.db.MediaResourceDao
import net.primal.android.feed.db.NostrResource
import net.primal.android.feed.db.NostrResourceDao
import net.primal.android.feed.db.PostDao
import net.primal.android.feed.db.PostData
import net.primal.android.feed.db.PostStats
import net.primal.android.feed.db.PostStatsDao
import net.primal.android.feed.db.RepostDao
import net.primal.android.feed.db.RepostData
import net.primal.android.profile.db.PostUserStats
import net.primal.android.profile.db.PostUserStatsDao
import net.primal.android.profile.db.ProfileMetadata
import net.primal.android.profile.db.ProfileMetadataDao
import net.primal.android.profile.db.ProfileStats
import net.primal.android.profile.db.ProfileStatsDao
import net.primal.android.serialization.RoomCustomTypeConverters
import net.primal.android.thread.db.ConversationCrossRef
import net.primal.android.thread.db.ConversationCrossRefDao
import net.primal.android.thread.db.ConversationDao

@Database(
    entities = [
        PostData::class,
        ProfileMetadata::class,
        RepostData::class,
        PostStats::class,
        MediaResource::class,
        Feed::class,
        FeedPostDataCrossRef::class,
        FeedPostRemoteKey::class,
        FeedPostSync::class,
        ConversationCrossRef::class,
        PostUserStats::class,
        TrendingHashtag::class,
        ProfileStats::class,
        NostrResource::class,
    ],
    version = 6,
    exportSchema = true,
)
@TypeConverters(RoomCustomTypeConverters::class)
abstract class PrimalDatabase : RoomDatabase() {

    abstract fun profiles(): ProfileMetadataDao

    abstract fun posts(): PostDao

    abstract fun reposts(): RepostDao

    abstract fun postStats(): PostStatsDao

    abstract fun mediaResources(): MediaResourceDao

    abstract fun nostrResources(): NostrResourceDao

    abstract fun feeds(): FeedDao

    abstract fun feedsConnections(): FeedPostDataCrossRefDao

    abstract fun feedPosts(): FeedPostDao

    abstract fun feedPostsRemoteKeys(): FeedPostRemoteKeyDao

    abstract fun feedPostsSync(): FeedPostSyncDao

    abstract fun conversationConnections(): ConversationCrossRefDao

    abstract fun conversations(): ConversationDao

    abstract fun postUserStats(): PostUserStatsDao

    abstract fun trendingHashtags(): TrendingHashtagDao

    abstract fun profileStats(): ProfileStatsDao
}
