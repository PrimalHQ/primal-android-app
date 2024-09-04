package net.primal.android.notes.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Deprecated("Use Feed.")
@Entity
data class OldFeed(
    @PrimaryKey
    val directive: String,
    val name: String,
)
