package net.primal.android.core.compose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun PrimalLoadingButton(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    height: Dp = 58.dp,
    shape: Shape = AppTheme.shapes.extraLarge,
    text: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight = FontWeight.SemiBold,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    containerColor: Color = AppTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    disabledContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
) {
    PrimalFilledButton(
        modifier = modifier,
        height = height,
        onClick = onClick,
        shape = shape,
        enabled = enabled,
        textStyle = AppTheme.typography.bodyLarge,
        contentColor = contentColor,
        contentPadding = contentPadding,
        containerColor = containerColor,
        disabledContainerColor = disabledContainerColor,
        content = {
            if (loading) {
                LoadingContent()
            } else {
                if (text != null) {
                    IconText(
                        modifier = Modifier.background(Color.Unspecified),
                        text = text,
                        fontSize = fontSize,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        fontWeight = fontWeight,
                    )
                } else if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null)
                }
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
    val text: String? = "Hello Nostr",
    val icon: ImageVector? = null,
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
            PrimalLoadingButtonPreviewState(
                enabled = true,
                loading = false,
                text = null,
                icon = Icons.Outlined.AcUnit,
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
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        PrimalLoadingButton(
            modifier = Modifier.height(48.dp),
            onClick = { },
            enabled = state.enabled,
            loading = state.loading,
            text = state.text,
            icon = state.icon,
            leadingIcon = state.leadingIcon,
        )
    }
}
