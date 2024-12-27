package net.primal.android.thread.articles.details.ui.rendering

import android.content.ClipData
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager.widget.ViewPager.LayoutParams
import io.noties.markwon.Markwon
import net.primal.android.R

@Composable
fun MarkdownRenderer(
    markdown: String,
    markwon: Markwon,
    modifier: Modifier = Modifier,
    onHighlight: ((content: String, context: String) -> Unit)? = null,
    onQuoteHighlight: ((content: String, context: String) -> Unit)? = null,
    onCommentHighlight: ((content: String, context: String) -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    val menuItemLabelHighlight = stringResource(R.string.article_details_highlight_toolbar_highlight)
    val menuItemLabelQuote = stringResource(R.string.article_details_highlight_toolbar_quote)
    val menuItemLabelComment = stringResource(R.string.article_details_highlight_toolbar_comment)
    val menuItemLabelCopy = stringResource(R.string.article_details_highlight_toolbar_copy)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                setTextIsSelectable(true)
                setCustomSelectionActionModeCallback(
                    object : ActionMode.Callback {
                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            menu?.clear()
                            menu?.add(menuItemLabelHighlight)
                            menu?.add(menuItemLabelQuote)
                            menu?.add(menuItemLabelComment)
                            menu?.add(menuItemLabelCopy)
                            return true
                        }

                        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            menu?.clear()
                            menu?.add(menuItemLabelHighlight)
                            menu?.add(menuItemLabelQuote)
                            menu?.add(menuItemLabelComment)
                            menu?.add(menuItemLabelCopy)
                            return true
                        }

                        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                            val selectedText = text.substring(selectionStart, selectionEnd)

                            val paragraphStart = text
                                .lastIndexOf('\n', selectionStart - 1)
                                .takeIf { it != -1 }?.plus(1) ?: 0

                            val paragraphEnd = text
                                .indexOf('\n', selectionEnd)
                                .takeIf { it != -1 } ?: text.length

                            val paragraph = text.substring(paragraphStart, paragraphEnd)

                            when (item?.title) {
                                menuItemLabelHighlight -> {
                                    onHighlight?.invoke(selectedText, paragraph)
                                }

                                menuItemLabelQuote -> {
                                    onQuoteHighlight?.invoke(selectedText, paragraph)
                                }

                                menuItemLabelComment -> {
                                    onCommentHighlight?.invoke(selectedText, paragraph)
                                }

                                menuItemLabelCopy -> {
                                    clipboardManager.setClip(ClipEntry(ClipData.newPlainText("", selectedText)))
                                }
                            }
                            mode?.finish()
                            return true
                        }

                        override fun onDestroyActionMode(mode: ActionMode?) = Unit
                    },
                )
            }
        },
        update = { textView ->
            markwon.setMarkdown(textView, markdown)
        },
    )
}
