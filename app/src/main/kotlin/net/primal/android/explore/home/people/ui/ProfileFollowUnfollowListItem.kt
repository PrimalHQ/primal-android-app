package net.primal.android.explore.home.people.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.FollowUnfollowButton
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.utils.shortened
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme

@Composable
fun ProfileFollowUnfollowListItem(
    data: UserProfileItemUi,
    onClick: () -> Unit,
    onFollowUnfollowClick: (isFollowed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 40.dp,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UniversalAvatarThumbnail(
            avatarSize = avatarSize,
            avatarCdnImage = data.avatarCdnImage,
            legendaryCustomization = data.legendaryCustomization,
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserDisplayNameInternetIdentifier(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f),
                displayName = data.displayName,
                internetIdentifier = data.internetIdentifier,
                legendaryCustomization = data.legendaryCustomization,
            )

            if (data.followersCount != null && data.followersCount != 0) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = data.followersCount.shortened(),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(id = R.string.search_followers_text).lowercase(),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        FollowUnfollowButton(
            modifier = Modifier
                .defaultMinSize(minWidth = 92.dp)
                .height(36.dp),
            isFollowed = data.isFollowed == true,
            onClick = { onFollowUnfollowClick(data.isFollowed == true) },
        )
    }
    PrimalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun UserDisplayNameInternetIdentifier(
    displayName: String,
    internetIdentifier: String?,
    legendaryCustomization: LegendaryCustomization?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        NostrUserText(
            displayName = displayName,
            displayNameFontWeight = FontWeight.SemiBold,
            style = AppTheme.typography.bodyLarge,
            internetIdentifier = internetIdentifier,
            legendaryCustomization = legendaryCustomization,
        )

        if (internetIdentifier != null && internetIdentifier.isNotEmpty()) {
            Text(
                text = internetIdentifier,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
