package net.primal.android.core.compose.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.theme.AppTheme

@Composable
fun NoteActionRow(
    onPhotosImported: (List<Uri>) -> Unit,
) {
    val photoImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = 5,
        )
    ) { uris -> onPhotosImported(uris) }

    Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        IconButton(
            onClick = {
                photoImportLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
        ) {
            Icon(
                imageVector = PrimalIcons.ImportPhotoFromGallery,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}
