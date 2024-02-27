package net.primal.android.wallet.transactions.send.create

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.wallet.domain.DraftTxStatus
import net.primal.android.wallet.transactions.send.create.ui.TransactionEditor
import net.primal.android.wallet.transactions.send.create.ui.TransactionFailed
import net.primal.android.wallet.transactions.send.create.ui.TransactionSending
import net.primal.android.wallet.transactions.send.create.ui.TransactionSuccess

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionScreen(viewModel: CreateTransactionViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    CreateTransactionScreen(
        state = state.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun CreateTransactionScreen(
    state: CreateTransactionContract.UiState,
    eventPublisher: (CreateTransactionContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            if (state.transaction.status != DraftTxStatus.Sent) {
                PrimalTopAppBar(
                    title = when (state.transaction.status) {
                        DraftTxStatus.Draft -> stringResource(id = R.string.wallet_create_transaction_draft_title)
                        DraftTxStatus.Sending -> stringResource(
                            id = R.string.wallet_create_transaction_sending_title,
                        )

                        DraftTxStatus.Sent -> stringResource(id = R.string.wallet_create_transaction_success_title)
                        DraftTxStatus.Failed -> stringResource(id = R.string.wallet_create_transaction_failed_title)
                    },
                    navigationIcon = PrimalIcons.ArrowBack,
                    navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                    showDivider = false,
                    onNavigationIconClick = onClose,
                )
            }
        },
        content = { paddingValues ->
            when (state.transaction.status) {
                DraftTxStatus.Draft -> {
                    TransactionEditor(
                        modifier = Modifier.fillMaxSize(),
                        paddingValues = paddingValues,
                        state = state,
                        eventPublisher = eventPublisher,
                        onCancelClick = onClose,
                    )
                }

                DraftTxStatus.Sending -> {
                    TransactionSending(
                        modifier = Modifier.fillMaxSize(),
                        amountInSats = state.transaction.amountSats.toLong(),
                        receiver = state.profileLightningAddress
                            ?: state.transaction.targetLud16
                            ?: state.transaction.targetLnUrl?.ellipsizeLnUrl()
                            ?: state.transaction.lnInvoiceData?.description
                            ?: state.transaction.targetOnChainAddress?.ellipsizeOnChainAddress(),
                    )
                }

                DraftTxStatus.Sent -> {
                    TransactionSuccess(
                        modifier = Modifier.fillMaxSize(),
                        amountInSats = state.transaction.amountSats.toLong(),
                        receiver = state.profileLightningAddress
                            ?: state.transaction.targetLud16
                            ?: state.transaction.targetLnUrl?.ellipsizeLnUrl()
                            ?: state.transaction.lnInvoiceData?.description
                            ?: state.transaction.targetOnChainAddress?.ellipsizeOnChainAddress(),
                        onDoneClick = onClose,
                    )
                }

                DraftTxStatus.Failed -> {
                    TransactionFailed(
                        modifier = Modifier.fillMaxSize(),
                        errorMessage = state.error?.message ?: stringResource(id = R.string.app_generic_error),
                        onCloseClick = onClose,
                    )
                }
            }
        },
    )
}

fun String.ellipsizeLnUrl() = this.ellipsizeMiddle(size = 8).lowercase()

fun String.ellipsizeOnChainAddress() = this.ellipsizeMiddle(size = 16)
