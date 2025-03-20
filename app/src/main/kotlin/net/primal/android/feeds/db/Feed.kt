package net.primal.android.feeds.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.primal.domain.FeedSpecKind

@Entity(indices = [Index(value = ["ownerId", "spec"], unique = true)])
data class Feed(
    @PrimaryKey(autoGenerate = true)
    val position: Int = 0,
    val ownerId: String,
    val spec: String,
    val specKind: FeedSpecKind,
    val feedKind: String,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
)
