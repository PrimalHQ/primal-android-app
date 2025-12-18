package net.primal.android.core.compose.signer.model

import net.primal.domain.account.model.TrustLevel

data class SignerConnectCallbacks(
    val onConnectClick: () -> Unit,
    val onCancelClick: () -> Unit,
    val onTabChange: (SignerConnectTab) -> Unit,
    val onAccountSelect: (String) -> Unit,
    val onTrustLevelSelect: (TrustLevel) -> Unit,
    val onErrorDismiss: () -> Unit,
    // val onDailyBudgetClick: () -> Unit,
    // val onDailyBudgetChange: (Long?) -> Unit,
    // val onApplyDailyBudget: () -> Unit,
    // val onCancelDailyBudget: () -> Unit,
)
