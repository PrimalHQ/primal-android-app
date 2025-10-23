package net.primal.android.core.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NostrConnectBottomSheet(
    name: String?,
    url: String?,
    imageUrl: String?,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        ) {
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                ),
                leadingContent = {
                    UniversalAvatarThumbnail(
                        avatarSize = 40.dp,
                        avatarCdnImage = imageUrl?.let { CdnImage(sourceUrl = it) },
                    )
                },
                headlineContent = {
                    name
                },
                supportingContent = {
                    if (url != null) {
                        Text(
                            text = url,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                        )
                    }
                },
            )
        }
    }
}
