package net.primal.android.editor.ui.poll

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Close
import net.primal.android.editor.NoteEditorContract
import net.primal.android.editor.NoteEditorViewModel.Companion.MAX_POLL_CHOICE_LENGTH
import net.primal.android.theme.AppTheme

private val choiceFieldShape = PollEditorDefaults.choiceFieldShape
private val startPadding = PollEditorDefaults.startPadding
private val endPadding = PollEditorDefaults.endPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollChoiceField(
    choice: NoteEditorContract.PollChoice,
    index: Int,
    isFocused: Boolean,
    canRemove: Boolean,
    onTextChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = PrimalDefaults.outlinedTextFieldColors(
        focusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        unfocusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        focusedBorderColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        unfocusedBorderColor = AppTheme.colorScheme.outline,
    )
    val customSelectionColors = TextSelectionColors(
        handleColor = AppTheme.colorScheme.primary,
        backgroundColor = AppTheme.colorScheme.primary.copy(alpha = 0.2f),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
            BasicTextField(
                value = choice.text,
                onValueChange = { if (it.length <= MAX_POLL_CHOICE_LENGTH) onTextChange(it) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .onFocusChanged { onFocusChange(it.isFocused) },
                singleLine = true,
                textStyle = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(AppTheme.colorScheme.primary),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = choice.text,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        container = {
                            PollChoiceFieldContainer(
                                interactionSource = interactionSource,
                                colors = colors,
                            )
                        },
                        placeholder = { PollChoiceFieldPlaceholder(index = index) },
                        trailingIcon = { PollChoiceFieldLengthIndicator(isFocused = isFocused, choice = choice) },
                    )
                },
            )
        }

        if (canRemove) {
            RemovePollButton(onRemove = onRemove)
        }
    }
}

@Composable
private fun PollChoiceFieldLengthIndicator(isFocused: Boolean, choice: NoteEditorContract.PollChoice) {
    if (isFocused) {
        Text(
            text = "${choice.text.length}/$MAX_POLL_CHOICE_LENGTH",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        )
    }
}

@Composable
private fun PollChoiceFieldPlaceholder(index: Int) {
    Text(
        text = stringResource(
            id = R.string.poll_editor_choice_placeholder,
            index + 1,
        ),
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
    )
}

@Composable
private fun PollChoiceFieldContainer(interactionSource: MutableInteractionSource, colors: TextFieldColors) {
    OutlinedTextFieldDefaults.Container(
        enabled = true,
        isError = false,
        interactionSource = interactionSource,
        colors = colors,
        shape = choiceFieldShape,
    )
}

@Composable
private fun RemovePollButton(onRemove: () -> Unit) {
    IconButton(
        modifier = Modifier.size(32.dp),
        onClick = onRemove,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = PrimalIcons.Close,
            contentDescription = stringResource(id = R.string.accessibility_poll_remove_choice),
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        )
    }
}
