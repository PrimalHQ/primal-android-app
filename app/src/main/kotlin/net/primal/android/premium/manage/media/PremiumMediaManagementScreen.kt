package net.primal.android.premium.manage.media

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.ifNotNull
import net.primal.android.premium.manage.media.ui.MediaListItem
import net.primal.android.premium.manage.media.ui.TableHeader
import net.primal.android.premium.manage.media.ui.UsedStorageBreakdown
import net.primal.android.theme.AppTheme

@Composable
fun PremiumMediaManagementScreen(viewModel: PremiumMediaManagementViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    PremiumMediaManagementScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumMediaManagementScreen(state: PremiumMediaManagementContract.UiState, onClose: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_media_management_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .padding(paddingValues)
                .clip(
                    AppTheme.shapes.large.copy(
                        topStart = CornerSize(0.dp),
                        topEnd = CornerSize(0.dp),
                    ),
                ),
        ) {
            ifNotNull(state.usedStorageInBytes, state.maxStorageInBytes) { used, max ->
                item(key = "storageBreakdown") {
                    UsedStorageBreakdown(
                        modifier = Modifier.padding(bottom = 24.dp, top = 24.dp),
                        usedStorageInBytes = used,
                        maxStorageInBytes = max,
                        imagesInBytes = state.imagesInBytes,
                        videosInBytes = state.videosInBytes,
                        otherInBytes = state.otherInBytes,
                        calculating = state.calculating,
                    )
                }
            }

            item(key = "tableHeader") {
                TableHeader(
                    modifier = Modifier.clip(
                        AppTheme.shapes.large.copy(
                            bottomStart = CornerSize(0.dp),
                            bottomEnd = CornerSize(0.dp),
                        ),
                    ),
                )
            }

            items(
                items = state.mediaItems,
                key = { it.mediaId },
            ) { item ->
                PrimalDivider()
                MediaListItem(
                    item = item,
                    onCopyClick = {
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        val clip = ClipData.newPlainText("", item.mediaUrl)
                        clipboard.setPrimaryClip(clip)
                    },
                    onDeleteClick = {},
                )
            }
        }
    }
}
