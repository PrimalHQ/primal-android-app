package net.primal.android.core.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun IconText(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingIconSize: TextUnit = 24.sp,
    color: Color = LocalContentColor.current,
    leadingIconTintColor: Color? = color,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val contentText = buildAnnotatedString {
        if (leadingIcon != null) {
            appendInlineContent("icon", "[icon]")
            append("  ")
        }
        append(text)
    }

    val inlineContent = if (leadingIcon != null) {
        mapOf(
            "icon" to InlineTextContent(
                placeholder = Placeholder(
                    leadingIconSize, leadingIconSize, PlaceholderVerticalAlign.TextCenter,
                ),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        colorFilter = if (leadingIconTintColor != null) {
                            ColorFilter.tint(color = leadingIconTintColor)
                        } else {
                            null
                        },
                    )
                }
            },
        )
    } else {
        emptyMap()
    }

    Text(
        modifier = modifier,
        text = contentText,
        style = style,
        fontWeight = fontWeight,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = onTextLayout,
        inlineContent = inlineContent,
    )
}
