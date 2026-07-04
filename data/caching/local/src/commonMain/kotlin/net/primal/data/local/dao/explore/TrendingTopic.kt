package net.primal.data.local.dao.explore

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class TrendingTopic(
    @PrimaryKey
    val topic: String,
    val score: Float,
)
