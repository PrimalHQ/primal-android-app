package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Feed(
    @PrimaryKey
    val directive: String,
    val name: String,
)
