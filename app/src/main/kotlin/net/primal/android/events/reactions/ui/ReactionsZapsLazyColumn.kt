package net.primal.android.events.reactions.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.shortened
import net.primal.android.events.reactions.ReactionsContract
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.theme.AppTheme
import net.primal.core.utils.detectUrls

@Composable
fun ReactionsZapsLazyColumn(
    modifier: Modifier,
    state: ReactionsContract.UiState,
    onProfileClick: (profileId: String) -> Unit,
) {
    val pagingItems = state.zaps.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()
    LazyColumn(
        modifier = modifier,
        state = listState,
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
        ) {
            val item = pagingItems[it]
            when {
                item != null -> NoteZapListItem(
                    data = item,
                    onProfileClick = onProfileClick,
                )

                else -> Unit
            }
        }

        if (pagingItems.isEmpty()) {
            item(contentType = "NoContent") {
                ListNoContent(
                    modifier = Modifier.fillParentMaxSize(),
                    noContentText = stringResource(R.string.reactions_zaps_no_content),
                    refreshButtonVisible = false,
                )
            }
        }
    }
}

@Composable
private fun NoteZapListItem(data: EventZapUiModel, onProfileClick: (profileId: String) -> Unit) {
    val localUriHandler = LocalUriHandler.current

    Column {
        ListItem(
            modifier = Modifier.clickable {
                val messageUris = data.message?.detectUrls()

                if (messageUris?.isNotEmpty() == true) {
                    localUriHandler.openUriSafely(messageUris.first())
                } else {
                    onProfileClick(data.zapperId)
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = AppTheme.colorScheme.surfaceVariant,
            ),
            leadingContent = {
                UniversalAvatarThumbnail(
                    avatarCdnImage = data.zapperAvatarCdnImage,
                    avatarSize = 42.dp,
                    onClick = { onProfileClick(data.zapperId) },
                    legendaryCustomization = data.zapperLegendaryCustomization,
                )
            },
            headlineContent = {
                NostrUserText(
                    displayName = data.zapperName,
                    internetIdentifier = data.zapperInternetIdentifier,
                    legendaryCustomization = data.zapperLegendaryCustomization,
                )
            },
            supportingContent = {
                if (!data.message.isNullOrEmpty()) {
                    Text(
                        text = data.message,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                }
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
                        imageVector = PrimalIcons.FeedZaps,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.onSurfaceVariantAlt2),
                    )

                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = data.amountInSats.toLong().shortened(),
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colorScheme.onSurface,
                    )
                }
            },
        )
        PrimalDivider()
    }
}
