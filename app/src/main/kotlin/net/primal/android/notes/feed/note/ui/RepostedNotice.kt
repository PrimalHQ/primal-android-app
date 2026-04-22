package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedReposts
import net.primal.android.theme.AppTheme

@Composable
fun RepostedNotice(
    modifier: Modifier,
    repostedByAuthor: String,
    onRepostAuthorClick: (() -> Unit)? = null,
) {
    val contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = onRepostAuthorClick != null,
            onClick = { onRepostAuthorClick?.invoke() },
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.size(16.dp),
            imageVector = PrimalIcons.FeedReposts,
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = contentColor),
        )
        Spacer(modifier = Modifier.width(5.5.dp))
        Text(
            text = "$repostedByAuthor ${stringResource(id = R.string.feed_reposted_suffix)}",
            style = AppTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}
