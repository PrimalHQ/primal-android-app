package net.primal.android.explore.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.shortened
import net.primal.android.theme.AppTheme

@Composable
fun UserProfileListItem(data: UserProfileUi, onClick: (String) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick(data.profileId) },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            AvatarThumbnail(avatarCdnImage = data.avatarCdnImage)
        },
        headlineContent = {
            Text(
                text = data.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            if (!data.internetIdentifier.isNullOrEmpty()) {
                Text(
                    text = data.internetIdentifier.formatNip05Identifier(),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                )
            }
        },
        trailingContent = {
            if (data.followersCount != null) {
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = data.followersCount.shortened(),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.search_followers_text).lowercase(),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    )
                }
            }
        },
    )
}
