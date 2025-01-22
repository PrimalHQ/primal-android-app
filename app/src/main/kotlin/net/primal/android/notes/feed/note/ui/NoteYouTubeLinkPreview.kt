package net.primal.android.notes.feed.note.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.theme.AppTheme

@Composable
fun NoteYouTubeLinkPreview(
    modifier: Modifier,
    url: String,
    title: String?,
    thumbnailUrl: String?,
    thumbnailImageSizeDp: DpSize,
    onClick: (() -> Unit)? = null,
) {
    val density = LocalDensity.current
    val youTubeVideoId = url.extractYouTubeVideoId()

    Box(modifier = modifier) {
        var embeddedWebState by remember { mutableStateOf(EmbeddedWebPageState.Idle) }
        var previewSize by remember { mutableStateOf(DpSize(width = 0.dp, height = 0.dp)) }

        if (embeddedWebState == EmbeddedWebPageState.Ready || embeddedWebState == EmbeddedWebPageState.Initializing) {
            if (youTubeVideoId != null) {
                NoteEmbeddedWebPagePreview(
                    modifier = Modifier
                        .size(size = previewSize)
                        .clip(AppTheme.shapes.small),
                    url = "https://www.youtube.com/embed/$youTubeVideoId?autoplay=1&fs=0&iv_load_policy=3&loop=1",
                    state = embeddedWebState,
                    onStateChanged = { embeddedWebState = it },
                    pageLoadedReadyDelayMillis = 400.milliseconds.inWholeMilliseconds,
                )
            }
        }

        if (embeddedWebState == EmbeddedWebPageState.Idle || embeddedWebState == EmbeddedWebPageState.Initializing) {
            NoteVideoLinkPreview(
                modifier = Modifier.onSizeChanged {
                    with(density) {
                        previewSize = DpSize(width = it.width.toDp(), height = it.height.toDp())
                    }
                },
                url = url,
                title = title,
                thumbnailUrl = thumbnailUrl,
                thumbnailImageSize = thumbnailImageSizeDp,
                type = NoteAttachmentType.YouTube,
                loading = embeddedWebState == EmbeddedWebPageState.Initializing,
                onClick = {
                    if (youTubeVideoId != null) {
                        embeddedWebState = EmbeddedWebPageState.Initializing
                    } else {
                        onClick?.invoke()
                    }
                },
            )
        }
    }
}

private fun String.extractYouTubeVideoId(): String? {
    val uri = Uri.parse(this)
    val path = uri.path ?: return null

    return when {
        path.contains("/shorts/") || path.contains("/live/") -> {
            uri.lastPathSegment
        }

        uri.host?.contains("youtube.com") == true -> {
            val queryParameters = uri.getQueryParameter("v")
            queryParameters
        }

        uri.host == "youtu.be" -> {
            uri.pathSegments.firstOrNull()
        }

        else -> null
    }
}
