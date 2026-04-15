package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.editor.ui.annotateWithTaggedUsers
import net.primal.android.editor.ui.processUserTagChange
import net.primal.android.theme.AppTheme

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    textStyle: TextStyle = AppTheme.typography.bodyMedium,
    shape: Shape = CompactTextFieldDefaults.Shape,
    contentPadding: PaddingValues = CompactTextFieldDefaults.ContentPadding,
    maxLines: Int = 10,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        capitalization = KeyboardCapitalization.Sentences,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        maxLines = maxLines,
        textStyle = textStyle.copy(color = AppTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(AppTheme.colorScheme.primary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            CompactTextFieldDecorationBox(
                isEmpty = value.isEmpty(),
                enabled = enabled,
                shape = shape,
                contentPadding = contentPadding,
                placeholder = if (placeholder.isNotEmpty()) {
                    {
                        Text(
                            text = placeholder,
                            maxLines = 1,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                            style = textStyle,
                        )
                    }
                } else {
                    null
                },
                innerTextField = innerTextField,
            )
        },
    )
}

@Composable
fun CompactTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = AppTheme.typography.bodyMedium,
    shape: Shape = CompactTextFieldDefaults.Shape,
    contentPadding: PaddingValues = CompactTextFieldDefaults.ContentPadding,
    maxLines: Int = 10,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        capitalization = KeyboardCapitalization.Sentences,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    taggedUsers: List<NoteTaggedUser> = emptyList(),
    taggedUserColor: Color = AppTheme.colorScheme.secondary,
    onUserTaggingModeChanged: ((enabled: Boolean) -> Unit)? = null,
    onUserTagSearch: ((query: String) -> Unit)? = null,
) {
    val annotatedValue = value.annotateWithTaggedUsers(
        taggedUsers = taggedUsers,
        highlightColor = taggedUserColor,
    )

    BasicTextField(
        modifier = modifier,
        value = annotatedValue,
        onValueChange = { newValue ->
            if (onUserTaggingModeChanged != null && onUserTagSearch != null) {
                processUserTagChange(
                    newValue = newValue,
                    onUserTaggingModeChanged = onUserTaggingModeChanged,
                    onUserTagSearch = onUserTagSearch,
                    onValueChange = onValueChange,
                )
            } else {
                onValueChange(newValue)
            }
        },
        enabled = enabled,
        maxLines = maxLines,
        textStyle = textStyle.copy(color = AppTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(AppTheme.colorScheme.primary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            CompactTextFieldDecorationBox(
                isEmpty = value.text.isEmpty(),
                enabled = enabled,
                shape = shape,
                contentPadding = contentPadding,
                placeholder = placeholder,
                trailingIcon = trailingIcon,
                innerTextField = innerTextField,
            )
        },
    )
}

@Composable
private fun CompactTextFieldDecorationBox(
    isEmpty: Boolean,
    enabled: Boolean,
    shape: Shape,
    contentPadding: PaddingValues,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    innerTextField: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .alpha(if (enabled) 1f else CompactTextFieldDefaults.DISABLED_ALPHA)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = shape,
            )
            .padding(contentPadding)
            .defaultMinSize(minHeight = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (isEmpty && placeholder != null) {
            placeholder()
        }
        innerTextField()
        if (trailingIcon != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                trailingIcon()
            }
        }
    }
}

object CompactTextFieldDefaults {
    val ContentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    val Shape: Shape = RoundedCornerShape(percent = 50)
    const val DISABLED_ALPHA = 0.38f
}
