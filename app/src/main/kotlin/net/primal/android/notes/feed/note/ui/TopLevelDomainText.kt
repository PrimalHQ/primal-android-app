package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.theme.AppTheme

@Composable
fun TopLevelDomainText(modifier: Modifier, attachmentType: NoteAttachmentType) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconResId = when (attachmentType) {
            NoteAttachmentType.YouTube -> R.drawable.logo_youtube
            NoteAttachmentType.Rumble -> R.drawable.logo_rumble
            NoteAttachmentType.Spotify -> R.drawable.logo_spotify
            NoteAttachmentType.Tidal -> if (isAppInDarkPrimalTheme()) {
                R.drawable.logo_tidal_dark
            } else {
                R.drawable.logo_tidal_light
            }

            else -> null
        }

        if (iconResId != null) {
            Image(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp),
                painter = painterResource(iconResId),
                contentDescription = null,
            )
        }

        val text = when (attachmentType) {
            NoteAttachmentType.YouTube -> "youtube.com"
            NoteAttachmentType.Rumble -> "rumble.com"
            NoteAttachmentType.Spotify -> "spotify.com"
            NoteAttachmentType.Tidal -> "tidal.com"
            else -> ""
        }
        IconText(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            text = text,
            maxLines = 1,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            style = AppTheme.typography.bodyMedium,
        )
    }
}
