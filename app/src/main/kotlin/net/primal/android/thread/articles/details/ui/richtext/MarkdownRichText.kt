@file:Suppress("detekt:all")

package net.primal.android.thread.articles.details.ui.richtext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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

// TODO Delete once refactor completed
// @Composable
// internal fun RichTextScope.MarkdownRichText(
//    astNode: AstNode,
//    modifier: Modifier = Modifier,
//    highlights: List<JoinedHighlightsUi> = emptyList(),
//    onHighlightClick: ((highlightedText: String) -> Unit)? = null,
// ) {
//    val isDarkTheme = LocalPrimalTheme.current.isDarkTheme
//
//    // Assume that only RichText nodes reside below this level.
//    val richText = remember(astNode, highlights) {
//        computeRichTextString(
//            astNode = astNode,
//            isDarkTheme = isDarkTheme,
//            highlights = highlights,
//        )
//    }
//
//    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
//    val highlightedLiterals = highlights.map { it.content }
//
//    val pressIndicator = Modifier.pointerInput(onHighlightClick, highlights) {
//        detectTapGestures { pos ->
//            layoutResult?.let { layoutResult ->
//                val charPosition = layoutResult.getOffsetForPosition(pos)
//                val scopedHighlights = TextMatcher(content = richText.text, texts = highlightedLiterals).matches()
//                val clickedHighlightTextMatch = scopedHighlights.firstOrNull {
//                    charPosition in it.startIndex..it.endIndex
//                }
//                if (clickedHighlightTextMatch != null) {
//                    onHighlightClick?.invoke(clickedHighlightTextMatch.value)
//                }
//            }
//        }
//    }
//
//    Box(modifier = modifier.then(pressIndicator)) {
//        AndroidView(
//            factory = { context ->
//                val density = context.resources.displayMetrics.density
//
//                val textView = TextView(context)
//                textView.setTextIsSelectable(true)
//                textView.setCustomSelectionActionModeCallback(
//                    object : ActionMode.Callback {
//                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//                            menu?.clear()
//                            menu?.add("Highlight")
//                            menu?.add("Quote")
//                            menu?.add("Comment")
//                            menu?.add("Copy")
//                            return true
//                        }
//
//                        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//                            return false
//                        }
//
//                        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
//                            Timber.e("${item?.itemId} clicked.")
//                            Timber.i(textView.text.substring(textView.selectionStart, textView.selectionEnd))
//                            mode?.finish()
//                            return true
//                        }
//
//                        override fun onDestroyActionMode(mode: ActionMode?) {
//                        }
//                    },
//                )
//
//
//                val markwon = Markwon.builder(context)
//                    .usePlugin(object : AbstractMarkwonPlugin() {
//                        override fun configureTheme(builder: MarkwonTheme.Builder) {
//
//                        }
//                    })
//                    .usePlugin(object : AbstractMarkwonPlugin() {
//                        override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
//                            builder.setFactory(Paragraph::class.java) { config, _ ->
//                                arrayOf(
//                                    AbsoluteSizeSpan(16, true),
//                                    CustomLineHeightSpan((28 * density).toInt()),
//                                    CustomTypefaceSpan.create(ResourcesCompat.getFont(context, R.font.nacelle_regular)!!),
//                                )
//                            }
//                        }
//                    })
//                    .build()
//                markwon.setMarkdown(textView, richText.text);
//
//                textView
//            },
//        )
//        Text(
//            text = richText,
//            onTextLayout = {
//                layoutResult = it
// //                onTextChanged?.invoke(it, richText.text)
//            },
//        )
//    }
// }
//
//
// private fun computeRichTextString(
//    astNode: AstNode,
//    isDarkTheme: Boolean,
//    highlights: List<JoinedHighlightsUi>,
// ): RichTextString {
//    val richTextStringBuilder = RichTextString.Builder()
//    val highlightedLiterals = highlights.map { it.content }
//
//    // Modified pre-order traversal with pushFormat, popFormat support.
//    var iteratorStack = listOf(
//        AstNodeTraversalEntry(
//            astNode = astNode,
//            isVisited = false,
//            formatIndex = null,
//        ),
//    )
//
//    while (iteratorStack.isNotEmpty()) {
//        val (currentNode, isVisited, formatIndex) = iteratorStack.first().copy()
//        iteratorStack = iteratorStack.drop(1)
//
//        if (!isVisited) {
//            val newFormatIndex = when (val currentNodeType = currentNode.type) {
//                is AstCode -> {
//                    richTextStringBuilder.withFormat(RichTextString.Format.Code) {
//                        append(currentNodeType.literal)
//                    }
//                    null
//                }
//
//                is AstEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Italic)
//                is AstStrikethrough -> richTextStringBuilder.pushFormat(
//                    RichTextString.Format.Strikethrough,
//                )
//
//                is AstImage -> {
//                    richTextStringBuilder.appendInlineContent(
//                        content = InlineContent(
//                            initialSize = {
//                                IntSize(128.dp.roundToPx(), 128.dp.roundToPx())
//                            },
//                        ) {
//                            RemoteImage(
//                                url = currentNodeType.destination,
//                                contentDescription = currentNodeType.title,
//                                modifier = Modifier.fillMaxWidth(),
//                                contentScale = ContentScale.Inside,
//                            )
//                        },
//                    )
//                    null
//                }
//
//                is AstLink -> {
//                    richTextStringBuilder.pushFormat(
//                        RichTextString.Format.Link(
//                            destination = currentNodeType.destination,
//                        ),
//                    )
//                }
//
//                is AstSoftLineBreak -> {
//                    richTextStringBuilder.append(" ")
//                    null
//                }
//
//                is AstHardLineBreak -> {
//                    richTextStringBuilder.append("\n")
//                    null
//                }
//
//                is AstStrongEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Bold)
//
//                is AstText -> {
//                    val literal = currentNodeType.literal
//                    richTextStringBuilder.append(literal)
//
//                    val matchedHighlights = TextMatcher(content = literal, texts = highlightedLiterals).matches()
//                    if (matchedHighlights.isNotEmpty()) {
//                        richTextStringBuilder.withAnnotatedString {
//                            matchedHighlights.forEach {
//                                addHighlightAnnotation(
//                                    textMatch = it,
//                                    highlightBackgroundColor = if (isDarkTheme) {
//                                        HighlightBackgroundDark.copy(alpha = 0.675f)
//                                    } else {
//                                        HighlightBackgroundLight.copy(alpha = 0.675f)
//                                    },
//                                    highlightForegroundColor = if (isDarkTheme) {
//                                        Color.White
//                                    } else {
//                                        Color.Black
//                                    },
//                                )
//                            }
//                        }
//                    }
//
//                    null
//                }
//
//                is AstLinkReferenceDefinition -> richTextStringBuilder.pushFormat(
//                    RichTextString.Format.Link(destination = currentNodeType.destination),
//                )
//
//                else -> null
//            }
//
//            iteratorStack = iteratorStack.addFirst(
//                AstNodeTraversalEntry(
//                    astNode = currentNode,
//                    isVisited = true,
//                    formatIndex = newFormatIndex,
//                ),
//            )
//
//            // Do not visit children of terminals such as Text, Image, etc.
//            if (!currentNode.isRichTextTerminal()) {
//                currentNode.childrenSequence(reverse = true).forEach {
//                    iteratorStack = iteratorStack.addFirst(
//                        AstNodeTraversalEntry(
//                            astNode = it,
//                            isVisited = false,
//                            formatIndex = null,
//                        ),
//                    )
//                }
//            }
//        }
//
//        if (formatIndex != null) {
//            richTextStringBuilder.pop(formatIndex)
//        }
//    }
//
//    return richTextStringBuilder.toRichTextString()
// }
//
// private data class AstNodeTraversalEntry(
//    val astNode: AstNode,
//    val isVisited: Boolean,
//    val formatIndex: Int?,
// )
//
// private inline fun <reified T> List<T>.addFirst(item: T): List<T> {
//    return listOf(item) + this
// }
//
// private const val HIGHLIGHT_ANNOTATION_TAG = "highlight"
//
// private fun AnnotatedString.Builder.addHighlightAnnotation(
//    highlightBackgroundColor: Color,
//    highlightForegroundColor: Color,
//    textMatch: TextMatch,
// ) {
//    addStyle(
//        style = SpanStyle(background = highlightBackgroundColor, color = highlightForegroundColor),
//        start = textMatch.startIndex,
//        end = textMatch.endIndex,
//    )
//    addStringAnnotation(
//        tag = HIGHLIGHT_ANNOTATION_TAG,
//        annotation = textMatch.value,
//        start = textMatch.startIndex,
//        end = textMatch.endIndex,
//    )
// }
