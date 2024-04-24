package net.primal.android.note.reactions

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.utils.shortened
import net.primal.android.note.ui.NoteZapUiModel
import net.primal.android.theme.AppTheme

@Composable
fun ReactionsScreen(
    viewModel: ReactionsViewModel,
    onClose: () -> Unit,
    onProfileClick: (profileId: String) -> Unit,
) {
    val state = viewModel.state.collectAsState()

    ReactionsScreen(
        state = state.value,
        onClose = onClose,
        onProfileClick = onProfileClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReactionsScreen(
    state: ReactionsContract.UiState,
    onClose: () -> Unit,
    onProfileClick: (profileId: String) -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.note_reactions_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
            )
        },
        content = { paddingValues ->
            val pagingItems = state.zaps.collectAsLazyPagingItems()
            val listState = pagingItems.rememberLazyListStatePagingWorkaround()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
            }
        },
    )
}

@Composable
private fun NoteZapListItem(data: NoteZapUiModel, onProfileClick: (profileId: String) -> Unit) {
    Column {
        ListItem(
            modifier = Modifier.clickable { onProfileClick(data.zapperId) },
            colors = ListItemDefaults.colors(
                containerColor = AppTheme.colorScheme.surfaceVariant,
            ),
            leadingContent = {
                AvatarThumbnail(
                    avatarCdnImage = data.zapperAvatarCdnImage,
                    avatarSize = 42.dp,
                    onClick = { onProfileClick(data.zapperId) },
                )
            },
            headlineContent = {
                NostrUserText(displayName = data.zapperName, internetIdentifier = data.zapperInternetIdentifier)
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
