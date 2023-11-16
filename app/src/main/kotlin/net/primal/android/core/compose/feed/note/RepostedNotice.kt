package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedReposts
import net.primal.android.theme.AppTheme

@Composable
fun RepostedNotice(
    modifier: Modifier,
    repostedByAuthor: String,
    onRepostAuthorClick: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2
        PrimalClickableText(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.TopStart),
            text = buildAnnotatedString {
                appendInlineContent("icon", "[icon]")
                append(
                    AnnotatedString(
                        text = "  $repostedByAuthor ${stringResource(
                            id = R.string.feed_reposted_suffix,
                        )} ",
                        spanStyle = SpanStyle(color = contentColor),
                    ),
                )
            },
            style = AppTheme.typography.bodyMedium,
            onClick = { _, _ ->
                onRepostAuthorClick()
            },
            inlineContent = mapOf(
                "icon" to InlineTextContent(
                    placeholder = Placeholder(16.sp, 16.sp, PlaceholderVerticalAlign.TextCenter),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = PrimalIcons.FeedReposts,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = contentColor),
                        )
                    }
                },
            ),
        )
    }
}
