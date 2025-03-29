package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import net.primal.android.notes.db.ReferencedHighlight
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.aTagToNaddr

val HighlightBackgroundDark = Color(0xFF2E3726)
val HighlightBackgroundLight = Color(0xFFE8F3E8)

@Composable
fun ReferencedHighlight(
    modifier: Modifier = Modifier,
    highlight: ReferencedHighlight,
    isDarkTheme: Boolean,
    onClick: (naddr: String) -> Unit,
) {
    val naddr = highlight.aTag.aTagToNaddr()?.toNaddrString()
    Text(
        modifier = modifier
            .padding(top = 2.dp)
            .clickable(
                enabled = naddr != null,
                onClick = { naddr?.let(onClick) },
            ),
        text = highlight.text,
        style = AppTheme.typography.bodyMedium.merge(
            background = if (isDarkTheme) HighlightBackgroundDark else HighlightBackgroundLight,
            color = AppTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            lineBreak = LineBreak.Paragraph,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeight = 2.em,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None,
            ),
        ),
    )
}
