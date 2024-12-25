package net.primal.android.thread.articles.details.ui.rendering

import android.graphics.Color
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import io.noties.markwon.AbstractMarkwonPlugin
import net.primal.android.notes.feed.note.ui.HighlightBackgroundDark
import net.primal.android.notes.feed.note.ui.HighlightBackgroundLight

class MarkwonHighlightsPlugin(
    isDarkTheme: Boolean,
    private val highlightWords: List<String>,
    private val onWordClick: (String) -> Unit,
) : AbstractMarkwonPlugin() {

    private val textColor = if (isDarkTheme) Color.WHITE else Color.BLACK

    private val backgroundColor = if (isDarkTheme) {
        HighlightBackgroundDark.toArgb()
    } else {
        HighlightBackgroundLight.toArgb()
    }

    override fun afterSetText(text: TextView) {
        val spannable = text.text as? Spannable ?: return
        highlightWords.forEach { word ->
            val start = spannable.toString().indexOf(word)
            if (start >= 0) {
                val end = start + word.length

                spannable.setSpan(
                    BackgroundColorSpan(backgroundColor),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )

                spannable.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onWordClick(word)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                            ds.color = textColor
                        }
                    },
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }

        text.movementMethod = NoTapIndicationLinkMovementMethod.getInstance()
    }
}

class NoTapIndicationLinkMovementMethod : LinkMovementMethod() {
    companion object {
        private var sInstance: NoTapIndicationLinkMovementMethod? = null

        fun getInstance(): NoTapIndicationLinkMovementMethod {
            if (sInstance == null) {
                sInstance = NoTapIndicationLinkMovementMethod()
            }
            return sInstance!!
        }
    }

    override fun onTouchEvent(
        widget: TextView,
        buffer: Spannable,
        event: MotionEvent,
    ): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            return true
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}
