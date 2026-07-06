package net.primal.data.local.dao.explore

import androidx.room3.Entity
import androidx.room3.Index
import net.primal.domain.links.CdnImage

@Entity(
    primaryKeys = ["identifier", "authorId"],
    indices = [
        Index(value = ["aTag"]),
    ],
)
data class FollowPackData(
    val aTag: String,
    val identifier: String,
    val authorId: String,
    val title: String,
    val coverCdnImage: CdnImage?,
    val description: String?,
    val updatedAt: Long,
    val profilesCount: Int,
)
