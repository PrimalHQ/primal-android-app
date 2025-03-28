@file:SuppressWarnings("MagicNumber")

package net.primal.android.thread.articles.details.ui.rendering

import android.graphics.Rect
import android.graphics.Typeface
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import coil.imageLoader
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.CoreProps.HEADING_LEVEL
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.CustomTypefaceSpan
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TableAwareMovementMethod
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.ImageSizeResolver
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.inlineparser.BangInlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParser
import io.noties.markwon.movement.MovementMethodPlugin
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.articles.highlights.JoinedHighlightsUi
import net.primal.android.theme.AppTheme
import org.commonmark.node.Heading
import org.commonmark.node.Paragraph
import org.commonmark.parser.Parser

private fun Dp.toPx(density: Float) = (this.value * density)

private fun TextUnit.toPx(density: Float): Int = (this.value * density).toInt()

@Composable
fun rememberPrimalMarkwon(
    showHighlights: Boolean = false,
    highlights: List<JoinedHighlightsUi> = emptyList(),
    onLinkClick: ((link: String) -> Unit)? = null,
    onHighlightClick: ((highlightedText: String) -> Unit)? = null,
): Markwon {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density

    val isDarkTheme = LocalPrimalTheme.current.isDarkTheme
    val contentAppearance = LocalContentDisplaySettings.current.contentAppearance
    val colorScheme = AppTheme.colorScheme
    val extraColorScheme = AppTheme.extraColorScheme

    val highlightWords by remember(highlights) { mutableStateOf(highlights.map { it.content }) }

    return remember(
        onLinkClick,
        showHighlights,
        highlightWords,
        onHighlightClick,
        isDarkTheme,
        contentAppearance,
        colorScheme,
        extraColorScheme,
    ) {
        Markwon.builder(context)
            .usePlugin(CorePlugin.create())
            .usePlugin(CoilImagesPlugin.create(context, context.imageLoader))
            .usePlugin(SoftBreakAddsNewLinePlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(MovementMethodPlugin.create(TableAwareMovementMethod.create()))
            .usePlugin(
                MarkwonHighlightsPlugin(
                    isDarkTheme = isDarkTheme,
                    highlightWords = if (showHighlights) highlightWords else emptyList(),
                    onWordClick = { onHighlightClick?.invoke(it) },
                ),
            )
            .usePlugin(
                TablePlugin.create { builder ->
                    builder
                        .tableBorderColor(extraColorScheme.onSurfaceVariantAlt2.toArgb())
                        .tableBorderWidth((0.5 * density).toInt())
                        .tableCellPadding((6 * density).toInt())
                        .tableHeaderRowBackgroundColor(colorScheme.surface.toArgb())
                        .tableEvenRowBackgroundColor(colorScheme.surface.toArgb())
                        .tableOddRowBackgroundColor(colorScheme.surface.toArgb())
                },
            )
            .usePlugin(
                object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        builder
                            .blockQuoteColor(extraColorScheme.onSurfaceVariantAlt3.toArgb())
                            .blockQuoteWidth(4.dp.toPx(density).toInt())
                            .linkColor(colorScheme.secondary.toArgb())
                            .codeBackgroundColor(extraColorScheme.surfaceVariantAlt1.toArgb())
                            .codeTypeface(ResourcesCompat.getFont(context, R.font.fira_mono_regular)!!)
                            .codeTextColor(colorScheme.onSurface.toArgb())
                            .codeTextSize(contentAppearance.articleTextFontSize.toPx(density))
                            .codeBlockBackgroundColor(extraColorScheme.surfaceVariantAlt1.toArgb())
                            .codeBlockTypeface(ResourcesCompat.getFont(context, R.font.fira_mono_regular)!!)
                            .codeBlockTextColor(colorScheme.onSurface.toArgb())
                            .codeBlockTextSize(contentAppearance.articleTextFontSize.toPx(density))
                            .codeBlockMargin(16.dp.toPx(density).toInt())
                            .build()
                    }

                    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                        builder.linkResolver { _, link ->
                            onLinkClick?.invoke(link)
                        }

                        builder.imageSizeResolver(
                            object : ImageSizeResolver() {
                                override fun resolveImageSize(drawable: AsyncDrawable): Rect {
                                    return drawable.result.getBounds()
                                }
                            },
                        )
                    }

                    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                        builder.setFactory(Paragraph::class.java) { _, _ ->
                            arrayOf(
                                AbsoluteSizeSpan(contentAppearance.articleTextFontSize.value.toInt(), true),
                                CustomLineHeightSpan(contentAppearance.articleTextLineHeight.toPx(density)),
                                ForegroundColorSpan(extraColorScheme.onBrand.toArgb()),
                                CustomTypefaceSpan.create(
                                    ResourcesCompat.getFont(
                                        context,
                                        R.font.nacelle_regular,
                                    )!!,
                                ),
                            )
                        }

                        builder.setFactory(Heading::class.java) { _, props ->
                            val level = props.get(HEADING_LEVEL, 1)
                            arrayOf(
                                ForegroundColorSpan(colorScheme.onSurface.toArgb()),
                                StyleSpan(Typeface.BOLD),
                                AbsoluteSizeSpan(resolveHeadingFontSize(level).value.toInt(), true),
                                CustomLineHeightSpan(resolveHeadingLineHeight(level).toPx(density)),
                            )
                        }
                    }

                    override fun configureParser(builder: Parser.Builder) {
                        builder.inlineParserFactory(
                            MarkwonInlineParser.factoryBuilder()
                                .referencesEnabled(true)
                                .excludeInlineProcessor(BangInlineProcessor::class.java)
                                .build(),
                        )
                    }
                },
            )
            .build()
    }
}

private fun resolveHeadingFontSize(level: Int): TextUnit {
    return when (level) {
        0 -> 32.sp
        1 -> 28.sp
        2 -> 24.sp
        3 -> 22.sp
        4 -> 20.sp
        5 -> 18.sp
        else -> 16.sp
    }
}

private fun resolveHeadingLineHeight(level: Int): TextUnit {
    return when (level) {
        0 -> 38.sp
        1 -> 36.sp
        2 -> 32.sp
        3 -> 30.sp
        4 -> 28.sp
        5 -> 28.sp
        else -> 26.sp
    }
}
