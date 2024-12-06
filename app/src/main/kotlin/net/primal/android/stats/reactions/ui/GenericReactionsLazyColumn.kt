package net.primal.android.stats.reactions.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.stats.reactions.EventActionUi
import net.primal.android.theme.AppTheme

@Composable
fun GenericReactionsLazyColumn(
    modifier: Modifier,
    reactions: List<EventActionUi>,
    loading: Boolean,
    reactionIcon: ImageVector,
    noContentText: String,
    onProfileClick: (profileId: String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(items = reactions) { item ->
            Column {
                GenericReactionListItem(
                    item = item,
                    reactionIcon = reactionIcon,
                    onProfileClick = onProfileClick,
                )
                PrimalDivider()
            }
        }

        if (reactions.isEmpty()) {
            if (loading) {
                heightAdjustableLoadingLazyListPlaceholder(height = 56.dp)
            } else {
                item(contentType = "NoContent") {
                    ListNoContent(
                        modifier = Modifier.fillParentMaxSize(),
                        noContentText = noContentText,
                        refreshButtonVisible = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun GenericReactionListItem(
    item: EventActionUi,
    reactionIcon: ImageVector,
    onProfileClick: (profileId: String) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onProfileClick(item.profile.pubkey) },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            UniversalAvatarThumbnail(
                avatarCdnImage = item.profile.avatarCdnImage,
                avatarSize = 42.dp,
                onClick = { onProfileClick(item.profile.pubkey) },
                legendaryCustomization = item.profile.premiumDetails?.legendaryCustomization,
            )
        },
        headlineContent = {
            NostrUserText(
                displayName = item.profile.authorDisplayName,
                internetIdentifier = item.profile.internetIdentifier,
            )
        },
        trailingContent = {
            Column(
                modifier = Modifier.width(38.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .size(18.dp),
                    imageVector = reactionIcon,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.onSurfaceVariantAlt2),
                )
            }
        },
    )
}
