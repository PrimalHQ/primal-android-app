package net.primal.android.core.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconText(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    iconSize: TextUnit = 24.sp,
    color: Color = LocalContentColor.current,
    leadingIconTintColor: Color? = color,
    trailingIconTintColor: Color? = color,
    leadingIconContentDescription: String? = null,
    trailingIconContentDescription: String? = null,
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
            appendInlineContent("leadingIcon", "[leadingIcon]")
            append(" ")
        }
        append(text)
        if (trailingIcon != null) {
            append(" ")
            appendInlineContent("trailingIcon", "[trailingIcon]")
        }
    }

    val inlineContent = mutableMapOf<String, InlineTextContent>().apply {
        if (leadingIcon != null) {
            this["leadingIcon"] = buildIconInlineTextContent(
                iconSize = iconSize,
                icon = leadingIcon,
                iconTintColor = leadingIconTintColor,
                iconContentDescription = leadingIconContentDescription,
            )
        }

        if (trailingIcon != null) {
            this["trailingIcon"] = buildIconInlineTextContent(
                iconSize = iconSize,
                icon = trailingIcon,
                iconTintColor = trailingIconTintColor,
                iconContentDescription = trailingIconContentDescription,
            )
        }
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

@Composable
private fun buildIconInlineTextContent(
    iconSize: TextUnit,
    icon: ImageVector,
    iconTintColor: Color?,
    iconContentDescription: String? = null,
) = InlineTextContent(
    placeholder = Placeholder(
        iconSize,
        iconSize,
        PlaceholderVerticalAlign.TextCenter,
    ),
) {
    Box(
        modifier = Modifier.padding(bottom = 2.dp).fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = icon,
            contentDescription = iconContentDescription,
            colorFilter = if (iconTintColor != null) {
                ColorFilter.tint(color = iconTintColor)
            } else {
                null
            },
        )
    }
}
