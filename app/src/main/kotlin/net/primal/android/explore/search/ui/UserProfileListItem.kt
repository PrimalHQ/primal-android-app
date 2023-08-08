package net.primal.android.explore.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.utils.shortened
import net.primal.android.theme.AppTheme

@Composable
fun UserProfileListItem(
    data: UserProfileUi,
    onClick: (String) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onClick(data.profileId) },
        leadingContent = {
            AvatarThumbnailListItemImage(source = data.avatarUrl)
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
                    text = data.internetIdentifier,
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
                        text = "followers",
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    )
                }
            }
        }
    )
}
