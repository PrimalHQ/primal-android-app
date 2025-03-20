package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import net.primal.android.R
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Play
import net.primal.android.notes.feed.note.ui.attachment.NoteImageErrorImage
import net.primal.android.notes.feed.note.ui.attachment.NoteImageLoadingPlaceholder
import net.primal.android.theme.AppTheme
import net.primal.domain.EventUriType

@Composable
fun NoteAudioLinkPreview(
    modifier: Modifier = Modifier,
    title: String?,
    description: String?,
    thumbnailUrl: String?,
    eventUriType: EventUriType,
    onPlayClick: () -> Unit,
    loading: Boolean = false,
) {
    val previewHeight = when (eventUriType) {
        EventUriType.Spotify -> 150.dp
        EventUriType.Tidal -> 100.dp
        else -> 100.dp
    }
    Row(
        modifier = modifier
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                shape = AppTheme.shapes.small,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubcomposeAsyncImage(
            model = thumbnailUrl,
            modifier = Modifier
                .size(previewHeight)
                .clip(
                    shape = AppTheme.shapes.small.copy(
                        topEnd = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp),
                    ),
                ),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            loading = { NoteImageLoadingPlaceholder() },
            error = { NoteImageErrorImage() },
        )

        AudioInfoColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = previewHeight)
                .border(
                    width = 0.5.dp,
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = AppTheme.shapes.small.copy(
                        topStart = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp),
                    ),
                )
                .padding(start = 10.dp)
                .padding(vertical = 12.dp),
            title = title ?: stringResource(R.string.feed_note_render_unknown_audio_title),
            description = description,
            attachmentType = eventUriType,
            onPlayClick = onPlayClick,
            loading = loading,
        )
    }
}

@Composable
private fun AudioInfoColumn(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    attachmentType: EventUriType,
    loading: Boolean,
    onPlayClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        TopLevelDomainText(
            modifier = Modifier.padding(
                top = if (attachmentType == EventUriType.Tidal) 8.dp else 0.dp,
            ),
            eventUriType = attachmentType,
        )

        if (attachmentType != EventUriType.Tidal) {
            TitleDescriptionColumn(
                title = title,
                description = description,
            )
        }

        val buttonSize = if (attachmentType == EventUriType.Tidal) {
            DpSize(width = 88.dp, height = 36.dp)
        } else {
            DpSize(width = 72.dp, height = 32.dp)
        }

        Button(
            modifier = Modifier.size(buttonSize),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            enabled = !loading,
            onClick = onPlayClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colorScheme.onPrimary,
                contentColor = AppTheme.colorScheme.surfaceVariant,
            ),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = AppTheme.colorScheme.onSurface,
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        imageVector = PrimalIcons.Play,
                        contentDescription = null,
                        tint = AppTheme.colorScheme.surfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.feed_note_render_play_button),
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleDescriptionColumn(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
) {
    Column(
        modifier = modifier.padding(end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = AppTheme.typography.bodyMedium,
            fontSize = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = AppTheme.colorScheme.onPrimary,
        )
        if (description != null) {
            Text(
                text = description,
                color = if (isAppInDarkPrimalTheme()) {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt2
                },
                style = AppTheme.typography.bodySmall,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
