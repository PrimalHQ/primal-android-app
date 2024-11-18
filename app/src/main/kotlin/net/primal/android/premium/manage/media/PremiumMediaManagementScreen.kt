package net.primal.android.premium.manage.media

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.ifNotNull
import net.primal.android.premium.manage.media.ui.MediaListItem
import net.primal.android.premium.manage.media.ui.MediaUiItem
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
    val scope = rememberCoroutineScope()
    var deleteMediaItem by remember { mutableStateOf<MediaUiItem?>(null) }
    deleteMediaItem?.let { item ->
        DeleteUploadAlertDialog(
            item = item,
            onDismissRequest = { deleteMediaItem = null },
            onConfirm = {
                deleteMediaItem = null
                scope.launch {
                    Toast.makeText(context, "Delete not yet implemented.", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

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
                    onDeleteClick = { deleteMediaItem = item },
                )
            }
        }
    }
}

@Composable
private fun DeleteUploadAlertDialog(
    item: MediaUiItem,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.premium_media_management_delete_upload_dialog_title),
            )
        },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SubcomposeAsyncImage(
                    modifier = Modifier
                        .size(width = 64.dp, height = 48.dp)
                        .clip(AppTheme.shapes.extraSmall),
                    model = ImageRequest.Builder(context)
                        .data(item.thumbnailUrl ?: item.mediaUrl)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )

                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = stringResource(R.string.premium_media_management_delete_upload_dialog_text),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.premium_media_management_delete_upload_dialog_dismiss_button),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.premium_media_management_delete_upload_dialog_confirm_button),
                )
            }
        },
    )
}
