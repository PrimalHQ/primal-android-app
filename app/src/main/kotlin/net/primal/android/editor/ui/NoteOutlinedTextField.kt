package net.primal.android.editor.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import net.primal.android.core.utils.TextMatcher
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.theme.AppTheme

@Composable
fun NoteOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    taggedUsers: List<NoteTaggedUser>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        capitalization = KeyboardCapitalization.Sentences,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    taggedUserColor: Color = AppTheme.colorScheme.secondary,
    onUserTaggingModeChanged: (enabled: Boolean) -> Unit,
    onUserTagSearch: (query: String) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value.copy(
            annotatedString = value.text.asAnnotatedStringWithTaggedUsers(
                taggedUsers = taggedUsers,
                highlightColor = taggedUserColor,
            ),
        ),
        onValueChange = {
            val cursorPosition = it.selection.start
            val textUntilCursor = it.text.substring(startIndex = 0, endIndex = cursorPosition)
            val lastIndexOfUserTaggingSign = textUntilCursor.lastIndexOf("@")
            if (lastIndexOfUserTaggingSign != -1) {
                val query = it.text.substring(
                    startIndex = lastIndexOfUserTaggingSign + 1,
                    endIndex = cursorPosition,
                )

                if (query.hasStopCharacter()) {
                    onUserTaggingModeChanged(false)
                } else {
                    onUserTagSearch(query)
                }
            } else {
                onUserTaggingModeChanged(false)
            }

            onValueChange(it.copy(text = it.text))
        },
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = textStyle,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}

private fun String.hasStopCharacter(): Boolean {
    return when {
        contains(' ') -> true
        contains('\n') -> true
        contains('\t') -> true
        else -> false
    }
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
