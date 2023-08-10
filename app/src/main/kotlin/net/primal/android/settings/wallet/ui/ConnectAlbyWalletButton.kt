package net.primal.android.settings.wallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun ConnectAlbyWalletButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val buttonShape = AppTheme.shapes.medium
    Button(
        onClick = onClick,
        shape = buttonShape,
        enabled = enabled,
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
                                Color(0xFFFFDF6F),
                                Color(0xFFCF7828),
                            )
                        } else {
                            listOf(
                                Color(0xFFCCCCCC),
                                Color(0xFFCCCCCC),
                            )
                        }
                    ),
                    shape = buttonShape,
                )
            ),
        content = {
            if (loading) {
                LoadingContent()
            } else {
                IconText(
                    text = text,
                    modifier = Modifier.background(Color.Unspecified),
                    fontSize = fontSize,
                    leadingIcon = ImageVector.vectorResource(id = R.drawable.alby_logo),
                    leadingIconModifier = Modifier.height(100.dp).width(100.dp),
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
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

data class ConnectAlbyWalletButtonState(
    val enabled: Boolean,
    val loading: Boolean
)

class ConnectAlbyButtonStatePreviewProvider: PreviewParameterProvider<ConnectAlbyWalletButtonState> {
    override val values: Sequence<ConnectAlbyWalletButtonState>
        get() = sequenceOf(
            ConnectAlbyWalletButtonState(enabled = false, loading = false),
            ConnectAlbyWalletButtonState(enabled = false, loading = true),
            ConnectAlbyWalletButtonState(enabled = true, loading = false),
            ConnectAlbyWalletButtonState(enabled = true, loading = true)
        )
}

@Preview
@Composable
fun ConnectAlbyWalletButtonPreview(
    @PreviewParameter(ConnectAlbyButtonStatePreviewProvider::class)
    state: ConnectAlbyWalletButtonState
) {
    PrimalTheme {
        ConnectAlbyWalletButton(
            text = "Connect Alby Wallet",
            onClick = { },
            enabled = state.enabled,
            loading = state.loading
        )
    }
}