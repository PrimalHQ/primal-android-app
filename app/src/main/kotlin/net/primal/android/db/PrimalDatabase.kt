package net.primal.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.primal.android.feed.db.ConversationCrossRef
import net.primal.android.feed.db.ConversationCrossRefDao
import net.primal.android.feed.db.ConversationDao
import net.primal.android.feed.db.Feed
import net.primal.android.feed.db.FeedDao
import net.primal.android.feed.db.FeedPostDao
import net.primal.android.feed.db.FeedPostData
import net.primal.android.feed.db.FeedPostDataCrossRef
import net.primal.android.feed.db.FeedPostDataCrossRefDao
import net.primal.android.feed.db.FeedPostRemoteKey
import net.primal.android.feed.db.FeedPostRemoteKeyDao
import net.primal.android.feed.db.FeedPostSync
import net.primal.android.feed.db.FeedPostSyncDao
import net.primal.android.feed.db.PostDao
import net.primal.android.feed.db.PostData
import net.primal.android.feed.db.PostResource
import net.primal.android.feed.db.PostResourcesDao
import net.primal.android.feed.db.PostStats
import net.primal.android.feed.db.PostStatsDao
import net.primal.android.feed.db.RepostDao
import net.primal.android.feed.db.RepostData
import net.primal.android.profile.db.ProfileMetadata
import net.primal.android.profile.db.ProfileMetadataDao
import net.primal.android.serialization.RoomCustomTypeConverters

@Database(
    entities = [
        PostData::class,
        ProfileMetadata::class,
        RepostData::class,
        PostStats::class,
        PostResource::class,
        Feed::class,
        FeedPostDataCrossRef::class,
        FeedPostRemoteKey::class,
        FeedPostSync::class,
        ConversationCrossRef::class,
    ],
    views = [
        FeedPostData::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomCustomTypeConverters::class)
abstract class PrimalDatabase : RoomDatabase() {

    abstract fun profiles(): ProfileMetadataDao

    abstract fun posts(): PostDao

    abstract fun reposts(): RepostDao

    abstract fun postStats(): PostStatsDao

    abstract fun resources(): PostResourcesDao

    abstract fun feeds(): FeedDao

    abstract fun feedsConnections(): FeedPostDataCrossRefDao

    abstract fun feedPosts(): FeedPostDao

    abstract fun feedPostsRemoteKeys(): FeedPostRemoteKeyDao

    abstract fun feedPostsSync(): FeedPostSyncDao

    abstract fun conversationConnections(): ConversationCrossRefDao

    abstract fun conversations(): ConversationDao

}