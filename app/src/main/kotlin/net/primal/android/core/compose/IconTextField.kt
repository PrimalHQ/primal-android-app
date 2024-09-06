package net.primal.android.core.compose

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import net.primal.android.theme.AppTheme

@Composable
fun IconTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    placeholderTextColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    iconImageVector: ImageVector,
    iconTint: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    focusRequester: FocusRequester? = remember { FocusRequester() },
) {
    var focusRequested by rememberSaveable { mutableStateOf(false) }

    if (focusRequester != null) {
        LaunchedEffect(focusRequester) {
            if (!focusRequested) {
                focusRequester.requestFocus()
                focusRequested = true
            }
        }
    }

    OutlinedTextField(
        modifier = modifier
            .then(if (focusRequester != null) modifier.focusRequester(focusRequester) else modifier),
        value = value,
        onValueChange = onValueChange,
        shape = AppTheme.shapes.medium,
        colors = PrimalDefaults.outlinedTextFieldColors(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
        ),
        leadingIcon = {
            Icon(
                imageVector = iconImageVector,
                contentDescription = null,
                tint = iconTint,
            )
        },
        placeholder = {
            Text(
                text = placeholderText,
                color = placeholderTextColor,
                style = AppTheme.typography.bodyMedium,
            )
        },
    )
}
