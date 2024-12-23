package net.primal.android.thread.articles.details.ui.rendering

import android.graphics.Paint
import android.text.style.LineHeightSpan

class CustomLineHeightSpan(private val lineHeight: Int) : LineHeightSpan {
    override fun chooseHeight(
        text: CharSequence,
        start: Int,
        end: Int,
        spanstartv: Int,
        v: Int,
        fm: Paint.FontMetricsInt,
    ) {
        val originalHeight = fm.descent - fm.ascent
        if (originalHeight <= 0) return

        val ratio = lineHeight.toFloat() / originalHeight
        fm.descent = (fm.descent * ratio).toInt()
        fm.ascent = (fm.ascent * ratio).toInt()
        fm.bottom = fm.descent
        fm.top = fm.ascent
    }
}
