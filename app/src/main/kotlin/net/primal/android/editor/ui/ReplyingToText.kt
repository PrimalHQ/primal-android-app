package net.primal.android.editor.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun ReplyingToText(modifier: Modifier, replyToUsername: String) {
    val mention = "@$replyToUsername"
    val text = stringResource(id = R.string.thread_replying_to, mention)
    val contentText = buildAnnotatedString {
        append(text)
        addStyle(
            style = SpanStyle(
                color = AppTheme.colorScheme.secondary,
            ),
            start = text.indexOf(mention),
            end = text.length,
        )
    }

    Text(
        modifier = modifier,
        text = contentText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        style = AppTheme.typography.bodySmall,
    )
}
