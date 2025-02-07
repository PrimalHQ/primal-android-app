package net.primal.android.settings.muted.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["userId", "ownerId"])
data class MutedUserData(
    val userId: String,
    val ownerId: String,
    val userMetadataEventId: String? = null,
)
