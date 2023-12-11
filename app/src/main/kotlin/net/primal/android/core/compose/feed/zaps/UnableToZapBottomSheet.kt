package net.primal.android.core.compose.feed.zaps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AdjustTemporarilySystemBarColors
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.WalletPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnableToZapBottomSheet(
    zappingState: ZappingState,
    onDismissRequest: () -> Unit,
    onGoToWallet: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AdjustTemporarilySystemBarColors(
        navigationBarColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    )

    ModalBottomSheet(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            val warningMessage = if (!zappingState.walletConnected) {
                when (zappingState.walletPreference) {
                    WalletPreference.NostrWalletConnect -> stringResource(
                        id = R.string.zap_warning_bottom_sheet_enable_wallet_text,
                    )
                    else -> stringResource(id = R.string.zap_warning_bottom_sheet_activate_wallet_text)
                }
            } else {
                stringResource(id = R.string.zap_warning_bottom_sheet_no_sats_to_zap)
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(vertical = 16.dp),
                text = warningMessage,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.onSurface,
            )

            PrimalLoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(vertical = 24.dp)
                    .height(56.dp),
                text = stringResource(id = R.string.zap_warning_bottom_sheet_go_to_wallet_button),
                onClick = {
                    onDismissRequest()
                    onGoToWallet()
                },
            )
        }
    }
}
