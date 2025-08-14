package net.primal.android.profile.mention

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun TextFieldValue.appendUserTagAtSignAtCursorPosition(): TextFieldValue {
    val text = this.text
    val selection = this.selection

    val newText = if (selection.length > 0) {
        text.replaceRange(startIndex = selection.start, endIndex = selection.end, "@")
    } else {
        text.substring(0, selection.start) + "@" + text.substring(selection.start)
    }
    val newSelectionStart = selection.start + 1

    return this.copy(
        text = newText,
        selection = TextRange(start = newSelectionStart, end = newSelectionStart),
    )
}
