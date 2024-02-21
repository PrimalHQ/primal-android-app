package net.primal.android.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import net.primal.android.R
import net.primal.android.core.ext.openUriSafely
import net.primal.android.theme.AppTheme

private const val TOS_ANNOTATION_TAG = "TosAnnotationTag"
private const val PRIVACY_ANNOTATION_TAG = "PrivacyAnnotationTag"

private const val PRIMAL_TOS_URL = "https://www.primal.net/terms"
private const val PRIMAL_PRIVACY_POLICY_URL = "https://www.primal.net/privacy"

@Composable
fun ToSAndPrivacyPolicyText(modifier: Modifier = Modifier, tosPrefix: String) {
    val linkSpanStyle = SpanStyle(color = AppTheme.colorScheme.primary)
    val annotatedString = buildAnnotatedString {
        append(tosPrefix)
        append("\n")

        pushStringAnnotation(TOS_ANNOTATION_TAG, "tos")
        withStyle(style = linkSpanStyle) {
            append(stringResource(id = R.string.legal_tos_hint_highlighted_word))
        }
        pop()

        append(" and ")
        pushStringAnnotation(PRIVACY_ANNOTATION_TAG, "privacy")
        withStyle(style = linkSpanStyle) {
            append(stringResource(id = R.string.legal_privacy_policy_hint_highlighted_word))
        }
        append(".")
        pop()
    }

    val localUriHandler = LocalUriHandler.current
    PrimalClickableText(
        modifier = modifier,
        text = annotatedString,
        style = AppTheme.typography.bodySmall.copy(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            textAlign = TextAlign.Center,
        ),
        onClick = { position, offset ->
            annotatedString.getStringAnnotations(
                start = position,
                end = position,
            ).firstOrNull()?.let { annotation ->
                when (annotation.tag) {
                    TOS_ANNOTATION_TAG -> localUriHandler.openUriSafely(PRIMAL_TOS_URL)
                    PRIVACY_ANNOTATION_TAG -> localUriHandler.openUriSafely(PRIMAL_PRIVACY_POLICY_URL)
                }
            }
        },
    )
}
