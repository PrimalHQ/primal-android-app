package net.primal.data.repository.mappers.local

import net.primal.core.utils.asMapByKey
import net.primal.data.local.dao.explore.FollowPack as FollowPackPO
import net.primal.data.local.dao.explore.FollowPackData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.repository.mappers.authorNameUiFriendly
import net.primal.domain.explore.FollowPack as FollowPackDO
import net.primal.domain.explore.FollowPackProfileData

internal fun FollowPackData.asFollowPackDO(
    profiles: Map<String, ProfileData>,
    people: List<ProfileData>,
    followersCountMap: Map<String, Int>,
): FollowPackDO? {
    return FollowPackDO(
        identifier = identifier,
        title = title,
        coverCdnImage = coverCdnImage,
        description = description,
        authorId = authorId,
        authorProfileData = profiles[authorId]?.asFollowPackProfileData(
            followersCount = followersCountMap[authorId] ?: 0,
        ),
        profiles = people.map {
            it.asFollowPackProfileData(
                followersCount = followersCountMap[it.ownerId] ?: 0,
            )
        },
        profilesCount = profilesCount,
        updatedAt = updatedAt,
    )
}

internal fun FollowPackPO.asFollowPackDO(): FollowPackDO {
    val followersMap = stats.asMapByKey { it.profileId }
    return FollowPackDO(
        identifier = data.identifier,
        title = data.title,
        coverCdnImage = data.coverCdnImage,
        description = data.description,
        authorId = data.authorId,
        authorProfileData = author?.asFollowPackProfileData(
            followersCount = authorStats?.followers ?: 0,
        ),
        profiles = people.map {
            it.asFollowPackProfileData(
                followersCount = followersMap[it.ownerId]?.followers ?: 0,
            )
        },
        profilesCount = data.profilesCount,
        updatedAt = data.updatedAt,
    )
}

fun ProfileData.asFollowPackProfileData(followersCount: Int) =
    FollowPackProfileData(
        profileId = ownerId,
        displayName = authorNameUiFriendly(),
        internetIdentifier = internetIdentifier,
        followersCount = followersCount,
        avatarCdnImage = avatarCdnImage,
        primalPremiumInfo = primalPremiumInfo,
    )
