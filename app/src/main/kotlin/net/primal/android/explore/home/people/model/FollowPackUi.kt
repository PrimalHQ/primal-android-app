package net.primal.android.explore.home.people.model

import java.time.Instant
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.domain.explore.FollowPack
import net.primal.domain.links.CdnImage

data class FollowPackUi(
    val identifier: String,
    val coverCdnImage: CdnImage?,
    val title: String,
    val description: String?,
    val authorId: String,
    val authorProfileData: UserProfileItemUi?,
    val highlightedProfiles: List<UserProfileItemUi>,
    val profilesCount: Int,
    val profiles: List<UserProfileItemUi>,
    val updatedAt: Instant,
)

private const val HIGHLIGHTED_PROFILES_COUNT = 5

fun FollowPack.asFollowPackUi() =
    FollowPackUi(
        identifier = identifier,
        coverCdnImage = coverCdnImage,
        title = title,
        description = description,
        authorId = authorId,
        authorProfileData = authorProfileData?.mapAsUserProfileUi(),
        highlightedProfiles = profiles.take(HIGHLIGHTED_PROFILES_COUNT)
            .map { it.mapAsUserProfileUi() },
        profilesCount = this@asFollowPackUi.profilesCount,
        profiles = profiles.map { it.mapAsUserProfileUi() },
        updatedAt = Instant.ofEpochSecond(updatedAt),
    )
