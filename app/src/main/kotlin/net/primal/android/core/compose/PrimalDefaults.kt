package net.primal.android.core.compose

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.primal.android.theme.AppTheme

object PrimalDefaults {

    @Composable
    fun outlinedTextFieldColors(
        focusedContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
        unfocusedContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
        disabledContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
        errorContainerColor: Color = Color.Unspecified,
        errorBorderColor: Color = AppTheme.colorScheme.error.copy(alpha = 0.5f),
        focusedBorderColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
        unfocusedBorderColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
        disabledBorderColor: Color = AppTheme.colorScheme.outline,
        cursorColor: Color = Color.Unspecified,
    ): TextFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedContainerColor = focusedContainerColor,
            unfocusedContainerColor = unfocusedContainerColor,
            disabledContainerColor = disabledContainerColor,
            errorContainerColor = errorContainerColor,
            errorBorderColor = errorBorderColor,
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            disabledBorderColor = disabledBorderColor,
            cursorColor = cursorColor,
        )

    @Composable
    fun transparentOutlinedTextFieldColors(): TextFieldColors =
        outlinedTextFieldColors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
        )
}
