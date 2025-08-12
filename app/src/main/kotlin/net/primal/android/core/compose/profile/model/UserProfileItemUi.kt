package net.primal.android.core.compose.profile.model

import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.domain.common.UserProfileSearchItem
import net.primal.domain.explore.FollowPackProfileData
import net.primal.domain.links.CdnImage
import net.primal.domain.profile.ProfileData as ProfileDataDO

data class UserProfileItemUi(
    val profileId: String,
    val displayName: String,
    val internetIdentifier: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val avatarBlossoms: List<String> = emptyList(),
    val followersCount: Int? = null,
    val score: Float? = null,
    val isFollowed: Boolean? = null,
    val legendaryCustomization: LegendaryCustomization? = null,
    val isLive: Boolean = false,
)

fun UserProfileSearchItem.mapAsUserProfileUi() =
    UserProfileItemUi(
        profileId = this.metadata.profileId,
        displayName = this.metadata.authorNameUiFriendly(),
        internetIdentifier = this.metadata.internetIdentifier,
        avatarCdnImage = this.metadata.avatarCdnImage,
        avatarBlossoms = this.metadata.blossoms,
        followersCount = this.followersCount,
        score = this.score,
        legendaryCustomization = this.metadata.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
        isLive = this.isLive,
    )

fun FollowPackProfileData.mapAsUserProfileUi() =
    UserProfileItemUi(
        profileId = profileId,
        displayName = displayName,
        internetIdentifier = internetIdentifier,
        avatarCdnImage = avatarCdnImage,
        followersCount = followersCount,
        legendaryCustomization = primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
    )

fun ProfileDataDO.asUserProfileItemUi() =
    UserProfileItemUi(
        profileId = this.profileId,
        displayName = this.authorNameUiFriendly(),
        internetIdentifier = this.internetIdentifier,
        avatarCdnImage = this.avatarCdnImage,
        avatarBlossoms = this.blossoms,
        followersCount = null,
        score = null,
        isFollowed = null,
        legendaryCustomization = this.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
    )
