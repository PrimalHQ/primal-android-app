package net.primal.android.user.db

import androidx.room.Entity
import net.primal.android.user.domain.RelayKind

@Entity(
    primaryKeys = ["userId", "kind", "url"],
)
data class Relay(
    val userId: String,
    val kind: RelayKind,
    val url: String,
    val read: Boolean,
    val write: Boolean,
)
