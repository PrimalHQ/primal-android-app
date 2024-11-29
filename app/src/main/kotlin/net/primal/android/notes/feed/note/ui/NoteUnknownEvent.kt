package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme

@Composable
fun NoteUnknownEvent(
    modifier: Modifier = Modifier,
    altDescription: String,
    icon: ImageVector = Icons.Outlined.Description,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .background(
                shape = AppTheme.shapes.medium,
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
            )
            .padding(all = 16.dp)
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )

        Text(
            modifier = Modifier.padding(start = 12.dp, top = 2.dp),
            text = altDescription,
            style = AppTheme.typography.bodyMedium,
            maxLines = 7,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun PreviewLightNoteInvoice() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunrise) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            NoteUnknownEvent(
                altDescription = "This is unknown event.",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDarkNoteInvoice() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            NoteUnknownEvent(
                altDescription = "This is unknown event with some very long alt description " +
                    "which is going to break into multiple lines for sure.",
            )
        }
    }
}
