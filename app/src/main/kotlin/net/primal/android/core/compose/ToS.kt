package net.primal.android.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.ext.openUriSafely
import net.primal.android.theme.AppTheme

private const val TOS_ANNOTATION_TAG = "TosAnnotationTag"
private const val PRIVACY_ANNOTATION_TAG = "PrivacyAnnotationTag"

private const val PRIMAL_TOS_URL = "https://www.primal.net/terms"
private const val PRIMAL_PRIVACY_POLICY_URL = "https://www.primal.net/privacy"

@Composable
fun ToSAndPrivacyPolicyText(
    tosPrefix: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    color: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    linksColor: Color = AppTheme.colorScheme.primary,
) {
    val linkSpanStyle = SpanStyle(color = linksColor, textDecoration = TextDecoration.Underline)
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
        pop()
    }

    val localUriHandler = LocalUriHandler.current
    PrimalClickableText(
        modifier = modifier,
        text = annotatedString,
        style = AppTheme.typography.bodySmall.copy(
            color = color,
            fontSize = fontSize,
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
