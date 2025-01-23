package net.primal.android.notes.feed.note.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.theme.AppTheme

private fun String.convertToSpotifyEmbedUrl(): String? {
    return try {
        val urlComponents = URL(this)
        val path = urlComponents.path ?: run {
            println("Unable to parse Spotify URL path")
            return null
        }

        // Split the path to extract type and ID
        val pathComponents = path.split("/").filter { it.isNotBlank() }
        if (pathComponents.size < 2) {
            println("Invalid Spotify URL path components")
            return null
        }

        val type = pathComponents[0]
        val id = pathComponents[1]

        return "https://open.spotify.com/embed/$type/$id?autoplay=1"
    } catch (e: Exception) {
        println("Invalid Spotify URL: ${e.message}")
        null
    }
}

@Composable
fun NoteAudioSpotifyLinkPreview(
    modifier: Modifier = Modifier,
    url: String,
    title: String?,
    description: String?,
    thumbnailUrl: String?,
    onPlayClick: () -> Unit,
) {
    val density = LocalDensity.current
    val embedUrl = url.convertToSpotifyEmbedUrl()

    Box(modifier = modifier.animateContentSize()) {
        var embeddedWebState by remember { mutableStateOf(EmbeddedWebPageState.Idle) }
        var previewSize by remember { mutableStateOf(DpSize(width = 0.dp, height = 0.dp)) }

        if (embeddedWebState == EmbeddedWebPageState.Ready || embeddedWebState == EmbeddedWebPageState.Initializing) {
            if (embedUrl != null) {
                NoteEmbeddedWebPagePreview(
                    modifier = Modifier
                        .clip(AppTheme.shapes.medium)
                        .size(size = previewSize),
                    url = embedUrl,
                    state = embeddedWebState,
                    onPageLoaded = { embeddedWebState = EmbeddedWebPageState.Ready },
                    pageLoadedReadyDelayMillis = 200.milliseconds.inWholeMilliseconds,
                )
            }
        }

        if (embeddedWebState == EmbeddedWebPageState.Idle || embeddedWebState == EmbeddedWebPageState.Initializing) {
            NoteAudioLinkPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged {
                        with(density) {
                            previewSize = DpSize(width = it.width.toDp(), height = it.height.toDp())
                        }
                    },
                title = title,
                description = description,
                thumbnailUrl = thumbnailUrl,
                attachmentType = NoteAttachmentType.Spotify,
                loading = embeddedWebState == EmbeddedWebPageState.Initializing,
                onPlayClick = {
                    if (embedUrl != null) {
                        embeddedWebState = EmbeddedWebPageState.Initializing
                    } else {
                        onPlayClick()
                    }
                },
            )
        }
    }
}
