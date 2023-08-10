package net.primal.android.core.compose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun PrimalOutlinedButton(
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.shapes.medium,
    enabled: Boolean = true,
    borderBrush: Brush = Brush.linearGradient(
        listOf(
            AppTheme.extraColorScheme.brand1,
            AppTheme.extraColorScheme.brand2,
        )
    ),
    disabledBorderBrush: Brush = borderBrush,
    contentColor: Color = AppTheme.colorScheme.onSurface,
    disabledContentColor: Color = contentColor.copy(alpha = 0.5f),
    textStyle: TextStyle = AppTheme.typography.bodySmall,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val buttonBorderBrush = rememberUpdatedState(
        if (enabled) borderBrush else disabledBorderBrush
    )
    val buttonContentColor = rememberUpdatedState(
        if (enabled) contentColor else disabledContentColor
    )

    Box(
        modifier = modifier
            .background(color = Color.Unspecified)
            .semantics { role = Role.Button }
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
            .border(width = 1.dp, brush = buttonBorderBrush.value, shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides buttonContentColor.value) {
            ProvideTextStyle(value = textStyle) {
                Row(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}
