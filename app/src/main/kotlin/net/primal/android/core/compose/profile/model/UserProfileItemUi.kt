package net.primal.android.core.compose.profile.model

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.explore.domain.UserProfileSearchItem

data class UserProfileItemUi(
    val profileId: String,
    val displayName: String,
    val internetIdentifier: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val followersCount: Int? = null,
    val score: Float? = null,
    val isFollowed: Boolean? = null,
)

fun UserProfileSearchItem.mapAsUserProfileUi() =
    UserProfileItemUi(
        profileId = this.metadata.ownerId,
        displayName = this.metadata.authorNameUiFriendly(),
        internetIdentifier = this.metadata.internetIdentifier,
        avatarCdnImage = this.metadata.avatarCdnImage,
        followersCount = this.followersCount,
        score = this.score,
    )
