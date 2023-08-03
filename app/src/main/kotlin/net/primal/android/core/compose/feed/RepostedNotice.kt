package net.primal.android.core.compose.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedReposts
import net.primal.android.theme.AppTheme

@Composable
fun RepostedNotice(
    repostedBy: String,
    onRepostAuthorClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {

        PrimalClickableText(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.TopStart),
            text = buildAnnotatedString {
                appendInlineContent("icon", "[icon]")
                append(' ')
                append(
                    AnnotatedString(
                        text = repostedBy,
                        spanStyle = SpanStyle(
                            color = AppTheme.colorScheme.primary,
                        )
                    )
                )
                append(' ')
                append(
                    AnnotatedString(
                        text = stringResource(id = R.string.feed_reposted_suffix),
                        spanStyle = SpanStyle(
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        )
                    )
                )
                append(' ')
            },
            style = AppTheme.typography.bodyMedium,
            onClick = { _, _ ->
                onRepostAuthorClick()
            },
            inlineContent = mapOf(
                "icon" to InlineTextContent(
                    placeholder = Placeholder(24.sp, 24.sp, PlaceholderVerticalAlign.TextCenter)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = PrimalIcons.FeedReposts,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2
                            ),
                        )
                    }
                }
            )
        )
    }
}
