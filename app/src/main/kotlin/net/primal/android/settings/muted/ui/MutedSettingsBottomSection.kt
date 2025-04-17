package net.primal.android.settings.muted.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalShapes

@Composable
fun MutedSettingsBottomSection(
    value: String,
    onValueChange: (String) -> Unit,
    sending: Boolean,
    onMute: () -> Unit,
    textFieldPlaceholder: String,
    modifier: Modifier = Modifier,
    showLeadingHashtag: Boolean = false,
    sendEnabled: Boolean = true,
) {
    val fieldHeight = 49.dp

    PrimalDivider()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1.0f)
                .height(fieldHeight),
            value = value,
            onValueChange = onValueChange,
            maxLines = 1,
            enabled = !sending,
            placeholder = {
                Text(
                    text = textFieldPlaceholder,
                    maxLines = 1,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyMedium,
                )
            },
            textStyle = AppTheme.typography.bodyMedium,
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.extraLarge,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
            ),
            leadingIcon = if (showLeadingHashtag) {
                {
                    Text(
                        text = "#",
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colorScheme.primary,
                    )
                }
            } else {
                null
            },
        )

        PrimalLoadingButton(
            modifier = Modifier
                .padding(start = 8.dp)
                .height(fieldHeight)
                .wrapContentWidth(),
            text = stringResource(
                id = R.string.settings_muted_accounts_mute_button,
            ).lowercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
            enabled = sendEnabled,
            onClick = onMute,
            shape = PrimalShapes.extraLarge,
        )
    }
}
