package net.primal.android.settings.wallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun ConnectThirdPartyWalletButton(
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
                        colors =
                        if (enabled) {
                            listOf(
                                Color(0xFFE5E5E5),
                                Color(0xFFE5E5E5),
                            )
                        } else {
                            listOf(
                                Color(0xFFA9A9A9),
                                Color(0xFFA9A9A9),
                            )
                        }
                    ),
                    shape = buttonShape,
                ).border(width = 1.dp, color = Color(0xFFC8C8C8), shape = buttonShape)
            ),
        content = {
            if (loading) {
                LoadingContent()
            } else {
                Text(
                    text = text,
                    modifier = Modifier.background(Color.Unspecified),
                    fontSize = fontSize,
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

data class ConnectThirdPartyWalletButtonState(
    val enabled: Boolean,
    val loading: Boolean
)

class ConnectThirdPartyButtonStatePreviewProvider:
    PreviewParameterProvider<ConnectThirdPartyWalletButtonState> {
    override val values: Sequence<ConnectThirdPartyWalletButtonState>
        get() = sequenceOf(
            ConnectThirdPartyWalletButtonState(enabled = false, loading = false),
            ConnectThirdPartyWalletButtonState(enabled = false, loading = true),
            ConnectThirdPartyWalletButtonState(enabled = true, loading = false),
            ConnectThirdPartyWalletButtonState(enabled = true, loading = true)
        )
}

@Preview
@Composable
fun ConnectThirdPartyWalletButtonPreview(
    @PreviewParameter(ConnectThirdPartyButtonStatePreviewProvider::class)
    state: ConnectThirdPartyWalletButtonState
) {
    PrimalTheme {
        ConnectThirdPartyWalletButton(
            text = "Connect Other Wallet",
            onClick = { },
            enabled = state.enabled,
            loading = state.loading
        )
    }
}