package net.primal.android.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import java.util.*
import net.primal.android.R
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.theme.AppTheme

@Composable
fun NoteAttachmentPreview(
    attachment: NoteAttachment,
    onDiscard: (UUID) -> Unit,
    onRetryUpload: (UUID) -> Unit,
) {
    val shape = AppTheme.shapes.medium
    Box(
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = attachment.localUri,
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .clip(shape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        if (attachment.remoteUrl == null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = shape,
                    ),
            )
        }

        MiniFloatingIconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            imageVector = Icons.Outlined.Close,
            onClick = { onDiscard(attachment.id) },
            floatingButtonContentDescription = stringResource(id = R.string.accessibility_close),
        )

        if (attachment.remoteUrl == null) {
            if (attachment.uploadError == null) {
                val uploaded = attachment.originalUploadedInBytes?.toFloat()
                val total = attachment.originalSizeInBytes?.toFloat()
                if (uploaded != null && total != null && (uploaded / total) < 1) {
                    CircularProgressIndicator(
                        progress = { (uploaded / total).coerceAtLeast(minimumValue = 0.05f) },
                        color = AppTheme.colorScheme.secondary,
                        trackColor = Color.Black,
                        strokeCap = StrokeCap.Round,
                    )
                } else {
                    CircularProgressIndicator(
                        color = AppTheme.colorScheme.secondary,
                        trackColor = Color.Black,
                        strokeCap = StrokeCap.Round,
                    )
                }
            } else {
                MiniFloatingIconButton(
                    modifier = Modifier.align(Alignment.Center),
                    imageVector = Icons.Outlined.Refresh,
                    floatingButtonContentDescription = stringResource(id = R.string.accessibility_refresh),
                    onClick = { onRetryUpload(attachment.id) },
                )
            }
        }
    }
}
