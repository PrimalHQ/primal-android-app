package net.primal.android.explore.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrendingTopic(
    @PrimaryKey
    val topic: String,
    val score: Float,
)
