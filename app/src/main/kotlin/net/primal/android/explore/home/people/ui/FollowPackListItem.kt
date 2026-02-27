package net.primal.android.explore.home.people.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.explore.home.people.model.FollowPackUi
import net.primal.android.explore.home.ui.FollowPackCoverImage
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.links.CdnImage

@Composable
fun FollowPackListItem(
    modifier: Modifier = Modifier,
    followPack: FollowPackUi,
    onClick: (profileId: String, identifier: String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.medium)
            .clickable { onClick(followPack.authorId, followPack.identifier) }
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
    ) {
        FollowPackCoverImage(
            height = 120.dp,
            coverImage = followPack.coverCdnImage,
        )
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = followPack.title,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            followPack.authorProfileData?.let {
                FollowPackAuthorRow(author = followPack.authorProfileData, onProfileClick = onProfileClick)
            }

            ItemFooter(
                modifier = Modifier.padding(end = 4.dp),
                profiles = followPack.highlightedProfiles,
                profilesCount = followPack.profilesCount,
                updatedAt = followPack.updatedAt,
                onProfileClick = onProfileClick,
            )
        }
    }
}

@Composable
private fun FollowPackAuthorRow(
    modifier: Modifier = Modifier,
    author: UserProfileItemUi,
    onProfileClick: (String) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        UniversalAvatarThumbnail(
            onClick = { onProfileClick(author.profileId) },
            avatarSize = 24.dp,
            avatarCdnImage = author.avatarCdnImage,
            legendaryCustomization = author.legendaryCustomization,
        )

        NostrUserText(
            displayName = author.displayName,
            displayNameColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            displayNameFontWeight = FontWeight.SemiBold,
            style = AppTheme.typography.bodyMedium,
            internetIdentifier = author.internetIdentifier,
            legendaryCustomization = author.legendaryCustomization,
        )
    }
}

@Composable
fun ItemFooter(
    modifier: Modifier = Modifier,
    profiles: List<UserProfileItemUi>,
    profilesCount: Int,
    updatedAt: Instant,
    onProfileClick: (String) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnailsRow(
            avatarCdnImages = profiles.map { it.avatarCdnImage },
            avatarBorderColor = Color.Transparent,
            avatarLegendaryCustomizations = profiles.map { it.legendaryCustomization },
            onClick = { onProfileClick(profiles[it].profileId) },
        )

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = pluralStringResource(id = R.plurals.follow_pack_list_item_users, profilesCount, profilesCount),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
            Text(
                text = stringResource(id = R.string.follow_pack_list_item_updated_ago, updatedAt.asBeforeNowFormat()),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}

@Preview
@Composable
private fun FollowPackListItemPreview() {
    PrimalPreview(
        primalTheme = PrimalTheme.Midnight,
    ) {
        FollowPackListItem(
            onProfileClick = {},
            onClick = { _, _ -> },
            followPack = FollowPackUi(
                identifier = "",
                coverCdnImage = CdnImage("https://placehold.co/600x400"),
                title = "Freedom Tech Signal",
                description = "",
                authorProfileData = UserProfileItemUi(
                    profileId = "",
                    displayName = "ODELL",
                    internetIdentifier = "odell@primal.net",
                ),
                profiles = emptyList(),
                profilesCount = 148,
                highlightedProfiles = listOf(
                    UserProfileItemUi(
                        profileId = "",
                        displayName = "ODELL",
                    ),
                    UserProfileItemUi(
                        profileId = "",
                        displayName = "ODELL",
                    ),
                ),
                updatedAt = Instant.now(),
                authorId = "profileId",
            ),
        )
    }
}
