package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Feed(
    @PrimaryKey
    val hex: String,
    val name: String,
    val pubKey: String? = null,
)
