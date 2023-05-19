package net.primal.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.primal.android.feed.db.EventStats
import net.primal.android.feed.db.EventStatsDao
import net.primal.android.feed.db.Repost
import net.primal.android.feed.db.RepostDao
import net.primal.android.feed.db.ShortTextNote
import net.primal.android.feed.db.ShortTextNoteDao
import net.primal.android.profile.db.ProfileMetadata
import net.primal.android.profile.db.ProfileMetadataDao
import net.primal.android.serialization.RoomCustomTypeConverters

@Database(
    entities = [
        ShortTextNote::class,
        ProfileMetadata::class,
        Repost::class,
        EventStats::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomCustomTypeConverters::class)
abstract class PrimalDatabase : RoomDatabase() {

    abstract fun profiles(): ProfileMetadataDao

    abstract fun events(): ShortTextNoteDao
    
    abstract fun reposts(): RepostDao

    abstract fun eventStats(): EventStatsDao

}