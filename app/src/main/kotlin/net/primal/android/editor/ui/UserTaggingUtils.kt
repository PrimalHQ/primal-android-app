package net.primal.android.editor.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import net.primal.android.core.utils.TextMatcher
import net.primal.android.editor.domain.NoteTaggedUser

internal fun TextFieldValue.annotateWithTaggedUsers(
    taggedUsers: List<NoteTaggedUser>,
    highlightColor: Color,
): TextFieldValue {
    if (taggedUsers.isEmpty()) return this
    return copy(
        annotatedString = text.asAnnotatedStringWithTaggedUsers(
            taggedUsers = taggedUsers,
            highlightColor = highlightColor,
        ),
    )
}

internal fun processUserTagChange(
    newValue: TextFieldValue,
    onUserTaggingModeChanged: (enabled: Boolean) -> Unit,
    onUserTagSearch: (query: String) -> Unit,
    onValueChange: (TextFieldValue) -> Unit,
) {
    val cursorPosition = newValue.selection.start
    val textUntilCursor = newValue.text.substring(startIndex = 0, endIndex = cursorPosition)
    val lastAtSign = textUntilCursor.lastIndexOf("@")
    if (lastAtSign != -1) {
        val query = newValue.text.substring(startIndex = lastAtSign + 1, endIndex = cursorPosition)
        if (query.hasStopCharacter()) {
            onUserTaggingModeChanged(false)
        } else {
            onUserTagSearch(query)
        }
    } else {
        onUserTaggingModeChanged(false)
    }
    onValueChange(newValue.copy(text = newValue.text))
}

private fun String.hasStopCharacter(): Boolean {
    return contains(' ') || contains('\n') || contains('\t')
}

private fun String.asAnnotatedStringWithTaggedUsers(
    taggedUsers: List<NoteTaggedUser>,
    highlightColor: Color,
): AnnotatedString {
    val text = this
    return buildAnnotatedString {
        append(text)
        TextMatcher(content = text, texts = taggedUsers.map { it.displayUsername }).matches()
            .forEach {
                addStyle(
                    style = SpanStyle(color = highlightColor),
                    start = it.startIndex,
                    end = it.endIndex,
                )
                addStringAnnotation(
                    tag = "Mentions",
                    annotation = it.value,
                    start = it.startIndex,
                    end = it.endIndex,
                )
            }
    }
}
