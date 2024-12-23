package net.primal.android.thread.articles.details.ui.rendering

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager.widget.ViewPager.LayoutParams
import io.noties.markwon.Markwon
import timber.log.Timber

@Composable
fun MarkdownRenderer(
    markdown: String,
    markwon: Markwon,
    modifier: Modifier = Modifier,
) {
    SelectionContainer {
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
                                menu?.add("Highlight")
                                menu?.add("Quote")
                                menu?.add("Comment")
                                menu?.add("Copy")
                                return true
                            }

                            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                                menu?.clear()
                                menu?.add("Highlight")
                                menu?.add("Quote")
                                menu?.add("Comment")
                                menu?.add("Copy")
                                return true
                            }

                            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                                Timber.e("${item?.itemId} clicked.")
                                Timber.i(text.substring(selectionStart, selectionEnd))
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
}
