package net.primal.android.feeds.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.android.feeds.domain.FeedSpecKind

@Entity
data class Feed(
    @PrimaryKey
    val spec: String,
    val specKind: FeedSpecKind,
    val feedKind: String,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
)
