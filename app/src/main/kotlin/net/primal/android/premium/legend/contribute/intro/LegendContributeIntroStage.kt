package net.primal.android.premium.legend.contribute.intro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.legend.contribute.LegendContributeContract
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendContributeIntroStage(
    modifier: Modifier,
    onClose: () -> Unit,
    onNext: (LegendContributeContract.PaymentMethod) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.legend_contribution_intro_stage_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = true,
            )
        },
        bottomBar = {
            LegendContributeIntroStageBottomBar(onClose = onClose)
        },
    ) { paddingValues ->
        IntroContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            onNext = onNext,
        )
    }
}

@Composable
private fun LegendContributeIntroStageBottomBar(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.clickable { onClose() },
            text = stringResource(id = R.string.legend_contribution_cancel),
            color = AppTheme.colorScheme.onBackground,
            style = AppTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun IntroContent(modifier: Modifier = Modifier, onNext: (LegendContributeContract.PaymentMethod) -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
            onClick = { onNext(LegendContributeContract.PaymentMethod.OnChainBitcoin) },
        ) {
            Text(
                text = stringResource(id = R.string.legend_contribution_pay_with_on_chain_bitcoin_button),
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
            onClick = { onNext(LegendContributeContract.PaymentMethod.BitcoinLightning) },
        ) {
            Text(
                text = stringResource(id = R.string.legend_contribution_pay_with_bitcoin_lightning),
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Color.White,
            )
        }
    }
}
