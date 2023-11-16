package net.primal.android.core.compose.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun PrimalFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AppTheme.shapes.extraLarge,
    containerColor: Color = AppTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    disabledContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    disabledContentColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
    textStyle: TextStyle = AppTheme.typography.bodyLarge,
    border: BorderStroke = BorderStroke(0.dp, Color.Unspecified),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val buttonContainerColor = rememberUpdatedState(
        if (enabled) containerColor else disabledContainerColor,
    )
    val buttonContentColor = rememberUpdatedState(
        if (enabled) contentColor else disabledContentColor,
    )

    Box(
        modifier = modifier
            .semantics { role = Role.Button }
            .shadow(elevation = 0.dp, shape)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
            .background(color = buttonContainerColor.value, shape = shape)
            .border(border = border, shape = shape),
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
                    content = content,
                )
            }
        }
    }
}

data class PrimalButtonPreviewState(
    val enabled: Boolean,
)

class PrimalStatePreviewProvider : PreviewParameterProvider<PrimalButtonPreviewState> {
    override val values: Sequence<PrimalButtonPreviewState>
        get() = sequenceOf(
            PrimalButtonPreviewState(enabled = true),
            PrimalButtonPreviewState(enabled = false),
        )
}

@Preview
@Composable
fun PrimalButtonPreview(
    @PreviewParameter(PrimalStatePreviewProvider::class)
    state: PrimalButtonPreviewState,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        PrimalLoadingButton(
            modifier = Modifier.height(48.dp),
            onClick = { },
            enabled = state.enabled,
            text = "Hello Primal!",
        )
    }
}
