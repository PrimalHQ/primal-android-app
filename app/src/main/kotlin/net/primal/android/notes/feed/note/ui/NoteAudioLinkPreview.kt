package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import net.primal.android.R
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Play
import net.primal.android.core.compose.icons.primaliconpack.SpotifyLogoDark
import net.primal.android.core.compose.icons.primaliconpack.SpotifyLogoLight
import net.primal.android.core.compose.icons.primaliconpack.TidalLogo
import net.primal.android.notes.feed.note.ui.attachment.NoteImageErrorImage
import net.primal.android.notes.feed.note.ui.attachment.NoteImageLoadingPlaceholder
import net.primal.android.theme.AppTheme

val TIDAL_DARK_TINT = Color(0xFFD9D9D9)
val TIDAL_LIGHT_TINT = Color(0xFF111111)

@Composable
fun NoteAudioLinkPreview(
    modifier: Modifier = Modifier,
    title: String?,
    description: String?,
    thumbnailUrl: String?,
    attachmentType: NoteAttachmentType,
    onPlayClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.small)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubcomposeAsyncImage(
            model = thumbnailUrl,
            modifier = Modifier
                .size(150.dp)
                .aspectRatio(1f)
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
                .height(150.dp)
                .padding(vertical = 12.dp),
            title = title ?: stringResource(R.string.feed_note_render_unknown_audio_title),
            description = description,
            attachmentType = attachmentType,
            onPlayClick = onPlayClick,
        )
    }
}

@Composable
private fun AudioInfoColumn(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    attachmentType: NoteAttachmentType,
    onPlayClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        when (attachmentType) {
            NoteAttachmentType.Spotify -> SpotifyRow(modifier = Modifier.padding(top = 4.dp))
            NoteAttachmentType.Tidal -> TidalRow(modifier = Modifier.padding(top = 4.dp))
            else -> Unit
        }

        TitleDescriptionColumn(
            title = title,
            description = description,
        )

        Button(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(32.dp),
            onClick = onPlayClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colorScheme.onPrimary,
                contentColor = AppTheme.colorScheme.surfaceVariant,
            ),
        ) {
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

@Composable
private fun SpotifyRow(modifier: Modifier = Modifier) {
    val icon = if (isAppInDarkPrimalTheme()) {
        PrimalIcons.SpotifyLogoDark
    } else {
        PrimalIcons.SpotifyLogoLight
    }

    IconDomainRow(
        modifier = modifier,
        imageVector = icon,
        tint = Color.Unspecified,
        text = "spotify.com",
    )
}

@Composable
private fun TidalRow(modifier: Modifier = Modifier) {
    val tint = if (isAppInDarkPrimalTheme()) {
        TIDAL_DARK_TINT
    } else {
        TIDAL_LIGHT_TINT
    }

    IconDomainRow(
        modifier = modifier,
        imageVector = PrimalIcons.TidalLogo,
        tint = tint,
        text = "tidal.com",
    )
}

@Composable
private fun IconDomainRow(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    tint: Color,
    text: String,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
        verticalAlignment = Alignment.Bottom,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isAppInDarkPrimalTheme()) {
                AppTheme.extraColorScheme.onSurfaceVariantAlt3
            } else {
                AppTheme.extraColorScheme.onSurfaceVariantAlt2
            },
            style = AppTheme.typography.bodySmall,
        )
    }
}
