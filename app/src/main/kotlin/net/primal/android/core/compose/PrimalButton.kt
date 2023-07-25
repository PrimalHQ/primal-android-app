package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun PrimalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val buttonShape = AppTheme.shapes.medium
    Button(
        onClick = onClick,
        shape = buttonShape,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = Color.Unspecified,
            contentColor = AppTheme.colorScheme.onSurface,
            disabledContainerColor = Color.Unspecified,
            disabledContentColor = AppTheme.colorScheme.outline,
        ),
        modifier = modifier
            .then(
                Modifier.background(
                    brush = Brush.linearGradient(
                        colors = if (enabled) {
                            listOf(
                                AppTheme.extraColorScheme.brand1,
                                AppTheme.extraColorScheme.brand2,
                            )
                        } else {
                            listOf(
                                Color(0xFF181818),
                                Color(0xFF181818),
                            )
                        },
                    ),
                    shape = buttonShape,
                )
            ),
        enabled = enabled,
        content = {
            if (loading) {
                LoadingContent()
            } else {
                IconText(
                    modifier = Modifier.background(Color.Unspecified),
                    text = text,
                    fontSize = fontSize,
                    leadingIcon = leadingIcon,
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                )
            }
        }
    )
}

@Composable
private fun LoadingContent() {
    CircularProgressIndicator(
        modifier = Modifier.size(12.dp),
        strokeWidth = 2.dp,
        color = AppTheme.colorScheme.onSurface,
    )
}

data class PrimalButtonPreviewState(
    val enabled: Boolean,
    val loading: Boolean,
    val leadingIcon: ImageVector? = null,
)

class PrimalButtonStatePreviewProvider : PreviewParameterProvider<PrimalButtonPreviewState> {
    override val values: Sequence<PrimalButtonPreviewState>
        get() = sequenceOf(
            PrimalButtonPreviewState(enabled = true, loading = false),
            PrimalButtonPreviewState(
                enabled = true,
                loading = false,
                leadingIcon = Icons.Outlined.CopyAll
            ),
            PrimalButtonPreviewState(enabled = false, loading = true),
            PrimalButtonPreviewState(enabled = false, loading = false),
            PrimalButtonPreviewState(enabled = true, loading = true)
        )
}

@Preview
@Composable
fun PrimalButtonPreview(
    @PreviewParameter(PrimalButtonStatePreviewProvider::class)
    state: PrimalButtonPreviewState
) {
    PrimalTheme {
        PrimalButton(
            onClick = { },
            enabled = state.enabled,
            loading = state.loading,
            text = "Hello Primal!",
            leadingIcon = state.leadingIcon,
        )
    }
}
