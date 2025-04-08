package net.primal.android.core.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ConnectRelay
import net.primal.android.settings.network.TextSubSection
import net.primal.android.theme.AppTheme

@Composable
fun DecoratedSettingsOutlinedTextField(
    modifier: Modifier,
    value: String,
    onValueChanged: (String) -> Unit,
    title: String,
    supportingActionText: String,
    onActionClick: () -> Unit,
    onSupportActionClick: () -> Unit,
    buttonEnabled: Boolean,
    placeholderText: String,
    showSupportContent: Boolean = false,
) {
    Column(modifier = modifier) {
        TextSubSection(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            text = title.uppercase(),
        )
        SettingsOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            buttonEnabled = buttonEnabled,
            buttonContentDescription = stringResource(id = R.string.accessibility_connect_relay),
            onValueChange = onValueChanged,
            onButtonClick = onActionClick,
            placeholderText = placeholderText,
        )

        if (showSupportContent) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    modifier = Modifier.clickable { onSupportActionClick() },
                    text = supportingActionText.lowercase(),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun SettingsOutlinedTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    buttonEnabled: Boolean,
    buttonContentDescription: String,
    onButtonClick: () -> Unit,
    placeholderText: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1.0f),
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.medium,
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = AppTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = placeholderText,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyMedium,
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(
                onGo = { onButtonClick() },
            ),
        )

        AppBarIcon(
            modifier = Modifier.padding(bottom = 4.dp, start = 8.dp),
            icon = PrimalIcons.ConnectRelay,
            enabledBackgroundColor = AppTheme.colorScheme.primary,
            tint = Color.White,
            enabled = buttonEnabled,
            onClick = onButtonClick,
            appBarIconContentDescription = buttonContentDescription,
        )
    }
}
