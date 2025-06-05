package net.primal.domain.explore

import net.primal.domain.links.CdnImage

data class FollowPack(
    val identifier: String,
    val title: String,
    val coverCdnImage: CdnImage?,
    val description: String?,
    val authorId: String,
    val authorProfileData: FollowPackProfileData?,
    val profilesCount: Int,
    val profiles: List<FollowPackProfileData>,
    val updatedAt: Long,
)
