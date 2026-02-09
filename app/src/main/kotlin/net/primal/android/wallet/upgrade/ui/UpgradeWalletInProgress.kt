package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.ui.FlowStatusColumn
import net.primal.domain.wallet.migration.MigrationStep

@Composable
fun UpgradeWalletInProgress(modifier: Modifier = Modifier, currentStep: MigrationStep?) {
    Column(
        modifier = modifier.padding(top = 32.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowStatusColumn(
            modifier = Modifier.fillMaxHeight(fraction = 0.7f),
            icon = null,
            headlineText = null,
            supportText = currentStep?.toUserFriendlyDescription()
                ?: stringResource(id = R.string.wallet_upgrade_creating_spark),
        )

        Spacer(modifier = Modifier.height(70.dp))

        Text(
            text = stringResource(id = R.string.wallet_upgrade_keep_open),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MigrationStep.toUserFriendlyDescription(): String {
    return when (this) {
        MigrationStep.CREATING_WALLET -> stringResource(id = R.string.wallet_upgrade_step_creating_wallet)
        MigrationStep.REGISTERING_WALLET -> stringResource(id = R.string.wallet_upgrade_step_registering_wallet)
        MigrationStep.CHECKING_BALANCE -> stringResource(id = R.string.wallet_upgrade_step_checking_balance)
        MigrationStep.CREATING_INVOICE -> stringResource(id = R.string.wallet_upgrade_step_creating_invoice)
        MigrationStep.TRANSFERRING_FUNDS -> stringResource(id = R.string.wallet_upgrade_step_transferring_funds)
        MigrationStep.AWAITING_CONFIRMATION -> stringResource(id = R.string.wallet_upgrade_step_awaiting_confirmation)
        MigrationStep.CONFIGURING_WALLET -> stringResource(id = R.string.wallet_upgrade_step_configuring_wallet)
        MigrationStep.IMPORTING_HISTORY -> stringResource(id = R.string.wallet_upgrade_step_importing_history)
        MigrationStep.ACTIVATING_WALLET -> stringResource(id = R.string.wallet_upgrade_step_activating_wallet)
    }
}
