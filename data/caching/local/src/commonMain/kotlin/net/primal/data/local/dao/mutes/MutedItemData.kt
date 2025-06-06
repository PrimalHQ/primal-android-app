package net.primal.data.local.dao.mutes

import androidx.room.Entity

@Entity(
    primaryKeys = ["item", "ownerId", "type"],
)
data class MutedItemData(
    val item: String,
    val ownerId: String,
    val type: MutedItemType,
)
