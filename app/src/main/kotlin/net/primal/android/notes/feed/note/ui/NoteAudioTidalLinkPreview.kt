package net.primal.android.notes.feed.note.ui

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

private fun String.convertToTidalEmbedUrl(): String {
    var embedString = this
    embedString = embedString.replace("listen.tidal.com", "embed.tidal.com")
    embedString = embedString.replace("/playlist/", "/playlists/")
    return embedString
}

@Composable
fun NoteAudioTidalLinkPreview(
    modifier: Modifier = Modifier,
    url: String,
    title: String?,
    description: String?,
    thumbnailUrl: String?,
) {
    val density = LocalDensity.current
    val embedUrl = url.convertToTidalEmbedUrl()

    Box(modifier = modifier) {
        var embeddedWebState by remember { mutableStateOf(EmbeddedWebPageState.Idle) }
        var previewSize by remember { mutableStateOf(DpSize(width = 0.dp, height = 0.dp)) }

        if (embeddedWebState == EmbeddedWebPageState.Ready || embeddedWebState == EmbeddedWebPageState.Initializing) {
            NoteEmbeddedWebPagePreview(
                modifier = Modifier
                    .clip(AppTheme.shapes.medium)
                    .size(size = previewSize),
                url = embedUrl,
                state = embeddedWebState,
                onStateChanged = { embeddedWebState = it },
                pageLoadedReadyDelayMillis = 200.milliseconds.inWholeMilliseconds,
            )
        }

        if (embeddedWebState == EmbeddedWebPageState.Idle || embeddedWebState == EmbeddedWebPageState.Initializing) {
            NoteAudioLinkPreview(
                modifier = Modifier.onSizeChanged {
                    with(density) {
                        previewSize = DpSize(width = it.width.toDp(), height = it.height.toDp())
                    }
                },
                title = title,
                description = description,
                thumbnailUrl = thumbnailUrl,
                attachmentType = NoteAttachmentType.Spotify,
                onPlayClick = { embeddedWebState = EmbeddedWebPageState.Initializing },
            )
        }
    }
}
