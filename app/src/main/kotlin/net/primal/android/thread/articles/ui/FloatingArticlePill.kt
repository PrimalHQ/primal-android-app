package net.primal.android.thread.articles.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedReplies
import net.primal.android.core.compose.icons.primaliconpack.LightningBolt
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.shortened
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingArticlePill(
    modifier: Modifier = Modifier,
    commentsCount: Long?,
    satsZapped: Long?,
    onCommentsClick: (() -> Unit)? = null,
    onCommentsLongClick: (() -> Unit)? = null,
    onZapClick: (() -> Unit)? = null,
    onZapLongClick: (() -> Unit)? = null,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Surface(
        modifier = modifier
            .semantics { role = Role.Button }
            .height(52.dp),
        color = AppTheme.colorScheme.surface,
        contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        shape = AppTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = AppTheme.colorScheme.outline,
                    shape = AppTheme.shapes.extraLarge,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .combinedClickable(
                        enabled = onCommentsClick != null || onCommentsLongClick != null,
                        onClick = { onCommentsClick?.invoke() },
                        onLongClick = onCommentsLongClick,
                    )
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconText(
                    text = commentsCount?.let { numberFormat.format(it) } ?: "0",
                    leadingIcon = PrimalIcons.FeedReplies,
                )
            }

            Box(
                modifier = Modifier
                    .combinedClickable(
                        enabled = onZapClick != null || onZapLongClick != null,
                        onClick = { onZapClick?.invoke() },
                        onLongClick = onZapLongClick,
                    )
                    .fillMaxHeight()
                    .padding(start = 0.dp, end = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconText(
                    text = satsZapped?.shortened() ?: "0",
                    leadingIcon = PrimalIcons.LightningBolt,
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@Preview
private fun PreviewFloatingArticlePill() {
    PrimalPreview(primalTheme = PrimalTheme.Sunrise) {
        Scaffold(
            floatingActionButton = {
                FloatingArticlePill(
                    commentsCount = 72,
                    satsZapped = 32768,
                )
            },
            content = {},
        )
    }
}
