package net.primal.android.core.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.theme.AppTheme

@Composable
fun WalletConnectionFooter(
    primaryButtonText: String,
    onPrimaryButtonClick: () -> Unit,
    secondaryButtonText: String,
    onSecondaryButtonClick: () -> Unit,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PrimalLoadingButton(
            text = primaryButtonText,
            enabled = enabled,
            loading = loading,
            onClick = {
                keyboardController?.hide()
                onPrimaryButtonClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 32.dp)
                .height(56.dp),
        )

        TextButton(
            onClick = onSecondaryButtonClick,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 48.dp),
                text = secondaryButtonText,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onPrimary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
