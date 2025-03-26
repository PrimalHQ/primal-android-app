package net.primal.android.core.compose.profile.model

import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.profile.db.ProfileData
import net.primal.domain.CdnImage
import net.primal.domain.UserProfileSearchItem

data class UserProfileItemUi(
    val profileId: String,
    val displayName: String,
    val internetIdentifier: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val followersCount: Int? = null,
    val score: Float? = null,
    val isFollowed: Boolean? = null,
    val legendaryCustomization: LegendaryCustomization? = null,
)

fun UserProfileSearchItem.mapAsUserProfileUi() =
    UserProfileItemUi(
        profileId = this.metadata.profileId,
        displayName = this.metadata.authorNameUiFriendly(),
        internetIdentifier = this.metadata.internetIdentifier,
        avatarCdnImage = this.metadata.avatarCdnImage,
        followersCount = this.followersCount,
        score = this.score,
        legendaryCustomization = this.metadata.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
    )

fun ProfileData.asUserProfileItemUi() =
    UserProfileItemUi(
        profileId = this.ownerId,
        displayName = this.authorNameUiFriendly(),
        internetIdentifier = this.internetIdentifier,
        avatarCdnImage = this.avatarCdnImage,
        followersCount = null,
        score = null,
        isFollowed = null,
        legendaryCustomization = this.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
    )
