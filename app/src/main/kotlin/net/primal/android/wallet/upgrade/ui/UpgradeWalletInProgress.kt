package net.primal.android.wallet.upgrade.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.wallet.ui.FlowStatusColumn
import net.primal.domain.wallet.migration.MigrationStep

@Composable
fun UpgradeWalletInProgress(modifier: Modifier = Modifier, currentStep: MigrationStep?) {
    val stepDescription = currentStep?.toUserFriendlyDescription()

    FlowStatusColumn(
        modifier = modifier,
        icon = null,
        headlineText = null,
        supportText = stepDescription ?: stringResource(id = R.string.wallet_upgrade_upgrading_support),
    )
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
