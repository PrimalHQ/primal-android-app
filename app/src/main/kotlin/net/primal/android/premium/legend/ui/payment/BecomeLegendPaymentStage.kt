package net.primal.android.premium.legend.ui.payment

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.legend.PremiumBecomeLegendContract
import net.primal.android.premium.legend.ui.BecomeLegendBottomBarButton
import net.primal.android.premium.legend.ui.PrimalLegendAmount
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.domain.Network
import net.primal.android.wallet.transactions.receive.QrCodeBox

@ExperimentalMaterial3Api
@Composable
fun BecomeLegendPaymentStage(
    modifier: Modifier,
    state: PremiumBecomeLegendContract.UiState,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_become_legend_payment_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = false,
            )
        },
        bottomBar = {
            BecomeLegendBottomBarButton(
                text = stringResource(R.string.premium_become_legend_button_copy_invoice),
                onClick = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    val clip = ClipData.newPlainText("", state.qrCodeValue)
                    clipboard.setPrimaryClip(clip)
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        ) {
            Box(modifier = Modifier.size(280.dp)) {
                QrCodeBox(
                    qrCodeValue = state.qrCodeValue,
                    network = Network.Bitcoin,
                )
            }

            PrimalLegendAmount(
                btcValue = state.selectedAmountInBtc,
                exchangeBtcUsdRate = state.exchangeBtcUsdRate,
            )

            Text(
                modifier = Modifier.padding(horizontal = 48.dp),
                text = stringResource(R.string.premium_become_legend_payment_instruction),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium,
                fontSize = 17.sp,
                lineHeight = 23.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
