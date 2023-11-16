package net.primal.android.core.compose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.IconText
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun PrimalLoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.shapes.extraLarge,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight = FontWeight.SemiBold,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    PrimalFilledButton(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        enabled = enabled,
        textStyle = AppTheme.typography.bodyLarge,
        contentPadding = contentPadding,
        content = {
            if (loading) {
                LoadingContent()
            } else {
                IconText(
                    modifier = Modifier.background(Color.Unspecified),
                    text = text,
                    fontSize = fontSize,
                    leadingIcon = leadingIcon,
                    fontWeight = fontWeight,
                )
            }
        },
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

data class PrimalLoadingButtonPreviewState(
    val enabled: Boolean,
    val loading: Boolean,
    val leadingIcon: ImageVector? = null,
)

class PrimalButtonStatePreviewProvider : PreviewParameterProvider<PrimalLoadingButtonPreviewState> {
    override val values: Sequence<PrimalLoadingButtonPreviewState>
        get() = sequenceOf(
            PrimalLoadingButtonPreviewState(enabled = true, loading = false),
            PrimalLoadingButtonPreviewState(
                enabled = true,
                loading = false,
                leadingIcon = Icons.Outlined.CopyAll,
            ),
            PrimalLoadingButtonPreviewState(enabled = false, loading = true),
            PrimalLoadingButtonPreviewState(enabled = false, loading = false),
            PrimalLoadingButtonPreviewState(enabled = true, loading = true),
        )
}

@Preview
@Composable
fun PrimalLoadingButtonPreview(
    @PreviewParameter(PrimalButtonStatePreviewProvider::class)
    state: PrimalLoadingButtonPreviewState,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        PrimalLoadingButton(
            modifier = Modifier.height(48.dp),
            onClick = { },
            enabled = state.enabled,
            loading = state.loading,
            text = "Hello Primal!",
            leadingIcon = state.leadingIcon,
        )
    }
}
