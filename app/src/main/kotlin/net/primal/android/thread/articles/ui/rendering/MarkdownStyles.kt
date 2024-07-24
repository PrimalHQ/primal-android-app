package net.primal.android.thread.articles.ui.rendering

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.ui.BlockQuoteGutter
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.HeadingStyle
import com.halilibo.richtext.ui.InfoPanelStyle
import com.halilibo.richtext.ui.ListStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.TableStyle
import com.halilibo.richtext.ui.string.RichTextStringStyle
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.theme.NacelleFontFamily

private val primalMarkdownBodyTextStyle = TextStyle(
    fontFamily = NacelleFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 28.sp,
)

private val primalMarkdownHeaderStyle: HeadingStyle = { level, textStyle ->
    when (level) {
        0 -> TextStyle(
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
        )

        1 -> TextStyle(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
        )

        2 -> TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
        )

        3 -> TextStyle(
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
        )

        4 -> TextStyle(
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        5 -> TextStyle(
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        else -> textStyle
    }
}

fun buildPrimalRichTextStyle(
    highlightColor: Color,
    codeBlockBackground: Color,
    codeBlockContent: Color,
    outlineColor: Color,
) = RichTextStyle(
    headingStyle = primalMarkdownHeaderStyle,
    stringStyle = RichTextStringStyle(
        boldStyle = null,
        italicStyle = null,
        underlineStyle = null,
        strikethroughStyle = null,
        subscriptStyle = primalMarkdownBodyTextStyle.toSpanStyle().copy(
            baselineShift = BaselineShift.Subscript,
            fontSize = 12.sp,
        ),
        superscriptStyle = primalMarkdownBodyTextStyle.toSpanStyle().copy(
            baselineShift = BaselineShift.Superscript,
            fontSize = 12.sp,
        ),
        codeStyle = primalMarkdownBodyTextStyle.toSpanStyle().copy(
            fontFamily = FontFamily(
                Font(resId = R.font.fira_mono_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
            ),

            color = codeBlockContent,
            background = codeBlockBackground,
        ),
        linkStyle = primalMarkdownBodyTextStyle.toSpanStyle().copy(
            color = highlightColor,
        ),
    ),
    paragraphSpacing = 20.sp,
    listStyle = ListStyle(
        markerIndent = null,
        contentsIndent = 8.sp,
        itemSpacing = 16.sp,
        orderedMarkers = null,
        unorderedMarkers = null,
    ),
    blockQuoteGutter = BlockQuoteGutter.BarGutter(
        startMargin = 6.sp,
        barWidth = 3.sp,
        endMargin = 6.sp,
        color = { it.copy(alpha = .25f) },
    ),
    codeBlockStyle = CodeBlockStyle(
        textStyle = TextStyle(
            fontFamily = FontFamily(
                Font(resId = R.font.fira_mono_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
            ),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            color = codeBlockContent,
        ),
        modifier = Modifier.background(color = codeBlockBackground, shape = RoundedCornerShape(12.dp)),
        padding = null,
        wordWrap = false,
    ),
    tableStyle = TableStyle(
        headerTextStyle = primalMarkdownBodyTextStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        cellPadding = 16.sp,
        borderColor = outlineColor,
        borderStrokeWidth = 2.0f,
    ),
    infoPanelStyle = InfoPanelStyle(
        contentPadding = null,
        background = null,
        textStyle = null,
    ),
)

@Composable
fun PrimalMarkdownStylesProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTextStyle provides primalMarkdownBodyTextStyle,
        LocalContentColor provides AppTheme.colorScheme.onSurface,
        content = content,
    )
}
