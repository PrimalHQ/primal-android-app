package net.primal.data.local.dao.explore

import androidx.room.Entity

@Entity(primaryKeys = ["ownerId", "query"])
data class RecentSearch(
    val ownerId: String,
    val query: String,
    val lastSearchedAt: Long,
)
