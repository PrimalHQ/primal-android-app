package net.primal.data.local.dao.mutes

import androidx.room3.Entity
import androidx.room3.Index

@Entity(
    primaryKeys = ["item", "ownerId", "type"],
    indices = [
        Index(value = ["ownerId", "type"]),
    ],
)
data class MutedItemData(
    val item: String,
    val ownerId: String,
    val type: MutedItemType,
    val listType: ListType,
)
