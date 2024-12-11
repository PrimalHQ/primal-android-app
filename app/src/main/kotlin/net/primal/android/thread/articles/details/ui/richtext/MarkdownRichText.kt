@file:Suppress("detekt:all")

package net.primal.android.thread.articles.details.ui.richtext

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.node.AstBlockQuote
import com.halilibo.richtext.markdown.node.AstCode
import com.halilibo.richtext.markdown.node.AstEmphasis
import com.halilibo.richtext.markdown.node.AstFencedCodeBlock
import com.halilibo.richtext.markdown.node.AstHardLineBreak
import com.halilibo.richtext.markdown.node.AstHeading
import com.halilibo.richtext.markdown.node.AstImage
import com.halilibo.richtext.markdown.node.AstIndentedCodeBlock
import com.halilibo.richtext.markdown.node.AstLink
import com.halilibo.richtext.markdown.node.AstLinkReferenceDefinition
import com.halilibo.richtext.markdown.node.AstListItem
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstParagraph
import com.halilibo.richtext.markdown.node.AstSoftLineBreak
import com.halilibo.richtext.markdown.node.AstStrikethrough
import com.halilibo.richtext.markdown.node.AstStrongEmphasis
import com.halilibo.richtext.markdown.node.AstText
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.FormattedList
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.InlineContent
import com.halilibo.richtext.ui.string.RichTextString
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.withFormat
import net.primal.android.LocalPrimalTheme
import net.primal.android.core.utils.TextMatch
import net.primal.android.core.utils.TextMatcher
import net.primal.android.highlights.model.HighlightUi
import net.primal.android.notes.feed.note.ui.HighlightBackgroundDark
import net.primal.android.notes.feed.note.ui.HighlightBackgroundLight

/**
 *
 * NOTE: this component is copied from: https://github.com/halilozercan/compose-richtext
 *       and later modified for our specific use case
 *
 * Only render the text content that exists below [astNode]. All the content blocks
 * like [AstBlockQuote] or [AstFencedCodeBlock] are ignored. This composable is
 * suited for [AstHeading] and [AstParagraph] since they are strictly text blocks.
 *
 * Some notes about commonmark and in general Markdown parsing.
 *
 * - Paragraph and Heading are the only RichTextString containers in base implementation.
 *   - RichTextString is build by traversing the children of Heading or Paragraph.
 *   - RichTextString can include;
 *     - Emphasis
 *     - StrongEmphasis
 *     - Image
 *     - Link
 *     - Code
 * - Code blocks should not have any children. Their whole content must reside in
 * [AstIndentedCodeBlock.literal] or [AstFencedCodeBlock.literal].
 * - Blocks like [BlockQuote], [FormattedList], [AstListItem] must have an [AstParagraph]
 * as a child to include any further RichText.
 * - CustomNode and CustomBlock can have their own scope, no idea about that.
 *
 * @param astNode Root node to accept as Text Content container.
 */
@Composable
internal fun RichTextScope.MarkdownRichText(
    astNode: AstNode,
    modifier: Modifier = Modifier,
    highlights: List<HighlightUi> = emptyList(),
) {
    val isDarkTheme = LocalPrimalTheme.current.isDarkTheme

    // Assume that only RichText nodes reside below this level.
    val richText = remember(astNode, highlights) {
        computeRichTextString(
            astNode = astNode,
            isDarkTheme = isDarkTheme,
            highlights = highlights,
        )
    }

    Text(text = richText, modifier = modifier)
}

private fun computeRichTextString(
    astNode: AstNode,
    isDarkTheme: Boolean,
    highlights: List<HighlightUi>,
): RichTextString {
    val richTextStringBuilder = RichTextString.Builder()
    val highlightedLiterals = highlights.map { it.content }

    // Modified pre-order traversal with pushFormat, popFormat support.
    var iteratorStack = listOf(
        AstNodeTraversalEntry(
            astNode = astNode,
            isVisited = false,
            formatIndex = null,
        ),
    )

    while (iteratorStack.isNotEmpty()) {
        val (currentNode, isVisited, formatIndex) = iteratorStack.first().copy()
        iteratorStack = iteratorStack.drop(1)

        if (!isVisited) {
            val newFormatIndex = when (val currentNodeType = currentNode.type) {
                is AstCode -> {
                    richTextStringBuilder.withFormat(RichTextString.Format.Code) {
                        append(currentNodeType.literal)
                    }
                    null
                }

                is AstEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Italic)
                is AstStrikethrough -> richTextStringBuilder.pushFormat(
                    RichTextString.Format.Strikethrough,
                )

                is AstImage -> {
                    richTextStringBuilder.appendInlineContent(
                        content = InlineContent(
                            initialSize = {
                                IntSize(128.dp.roundToPx(), 128.dp.roundToPx())
                            },
                        ) {
                            RemoteImage(
                                url = currentNodeType.destination,
                                contentDescription = currentNodeType.title,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Inside,
                            )
                        },
                    )
                    null
                }

                is AstLink -> {
                    richTextStringBuilder.pushFormat(
                        RichTextString.Format.Link(
                            destination = currentNodeType.destination,
                        ),
                    )
                }

                is AstSoftLineBreak -> {
                    richTextStringBuilder.append(" ")
                    null
                }

                is AstHardLineBreak -> {
                    richTextStringBuilder.append("\n")
                    null
                }

                is AstStrongEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Bold)

                is AstText -> {
                    val literal = currentNodeType.literal
                    richTextStringBuilder.append(literal)

                    val matchedHighlights = TextMatcher(content = literal, texts = highlightedLiterals).matches()
                    if (matchedHighlights.isNotEmpty()) {
                        richTextStringBuilder.withAnnotatedString {
                            matchedHighlights.forEach {
                                addHighlightAnnotation(
                                    textMatch = it,
                                    highlightBackgroundColor = if (isDarkTheme) {
                                        HighlightBackgroundDark
                                    } else {
                                        HighlightBackgroundLight
                                    },
                                    highlightForegroundColor = if (isDarkTheme) {
                                        Color.White
                                    } else {
                                        Color.Black
                                    },
                                )
                            }
                        }
                    }

                    null
                }

                is AstLinkReferenceDefinition -> richTextStringBuilder.pushFormat(
                    RichTextString.Format.Link(destination = currentNodeType.destination),
                )

                else -> null
            }

            iteratorStack = iteratorStack.addFirst(
                AstNodeTraversalEntry(
                    astNode = currentNode,
                    isVisited = true,
                    formatIndex = newFormatIndex,
                ),
            )

            // Do not visit children of terminals such as Text, Image, etc.
            if (!currentNode.isRichTextTerminal()) {
                currentNode.childrenSequence(reverse = true).forEach {
                    iteratorStack = iteratorStack.addFirst(
                        AstNodeTraversalEntry(
                            astNode = it,
                            isVisited = false,
                            formatIndex = null,
                        ),
                    )
                }
            }
        }

        if (formatIndex != null) {
            richTextStringBuilder.pop(formatIndex)
        }
    }

    return richTextStringBuilder.toRichTextString()
}

private data class AstNodeTraversalEntry(
    val astNode: AstNode,
    val isVisited: Boolean,
    val formatIndex: Int?,
)

private inline fun <reified T> List<T>.addFirst(item: T): List<T> {
    return listOf(item) + this
}

private const val HIGHLIGHT_ANNOTATION_TAG = "highlight"

private fun AnnotatedString.Builder.addHighlightAnnotation(
    highlightBackgroundColor: Color,
    highlightForegroundColor: Color,
    textMatch: TextMatch,
) {
    addStyle(
        style = SpanStyle(background = highlightBackgroundColor, color = highlightForegroundColor),
        start = textMatch.startIndex,
        end = textMatch.endIndex,
    )
    addStringAnnotation(
        tag = HIGHLIGHT_ANNOTATION_TAG,
        annotation = textMatch.value,
        start = textMatch.startIndex,
        end = textMatch.endIndex,
    )
}
