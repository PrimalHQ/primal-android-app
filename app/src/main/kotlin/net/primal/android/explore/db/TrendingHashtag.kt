package net.primal.android.explore.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrendingHashtag(
    @PrimaryKey
    val hashtag: String,
    val score: Float,
)
