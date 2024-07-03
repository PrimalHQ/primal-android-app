package net.primal.android.core.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import timber.log.Timber

@Composable
fun ImportPhotosIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color = LocalContentColor.current,
    onPhotosImported: (List<Uri>) -> Unit,
) {
    val multiplePhotosImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        Timber.e("uri = $uri")
        if (uri != null) onPhotosImported(listOf(uri))
    }

    IconButton(
        onClick = {
            multiplePhotosImportLauncher.launch(
                input = PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageAndVideo,
                ),
            )
        },
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
