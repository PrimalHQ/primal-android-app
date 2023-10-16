package net.primal.android.settings.muted.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MutedUserData(
    @PrimaryKey
    val userId: String,
    val userMetadataEventId: String? = null,
)
