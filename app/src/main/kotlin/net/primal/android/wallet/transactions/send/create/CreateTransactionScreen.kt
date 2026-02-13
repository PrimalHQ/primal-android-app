package net.primal.android.wallet.transactions.send.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.transactions.send.create.ui.TransactionEditor
import net.primal.android.wallet.transactions.send.create.ui.TransactionFailed
import net.primal.android.wallet.transactions.send.create.ui.TransactionSending
import net.primal.android.wallet.transactions.send.create.ui.TransactionSuccess
import net.primal.domain.wallet.DraftTxStatus

private const val TX_AMOUNT_KEY = "tx_amount"

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun CreateTransactionScreen(viewModel: CreateTransactionViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    CreateTransactionScreen(
        state = state.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@ExperimentalSharedTransitionApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun CreateTransactionScreen(
    state: CreateTransactionContract.UiState,
    eventPublisher: (CreateTransactionContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    PrimalScaffold(
        modifier = Modifier.imePadding(),
        topBar = { CreateTransactionTopAppBar(txStatus = state.transaction.status, onClose = onClose) },
        content = { paddingValues ->
            SharedTransitionLayout {
                AnimatedContent(
                    targetState = state.transaction.status,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TransactionStatus",
                ) { status ->
                    val sharedAmountModifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = TX_AMOUNT_KEY),
                        animatedVisibilityScope = this@AnimatedContent,
                    )

                    when (status) {
                        DraftTxStatus.Draft -> {
                            TransactionEditor(
                                modifier = Modifier
                                    .background(color = AppTheme.colorScheme.surfaceVariant)
                                    .fillMaxSize(),
                                paddingValues = paddingValues,
                                state = state,
                                eventPublisher = eventPublisher,
                                onCancelClick = onClose,
                                btcAmountModifier = sharedAmountModifier,
                            )
                        }

                        DraftTxStatus.Sending -> {
                            TransactionSending(
                                modifier = Modifier
                                    .background(color = AppTheme.colorScheme.surfaceVariant)
                                    .fillMaxSize(),
                                amountInSats = state.transaction.amountSats.toLong(),
                                sendingCompleted = state.sendingCompleted,
                                onAnimationFinished = {
                                    eventPublisher(CreateTransactionContract.UiEvent.SendingAnimationFinished)
                                },
                                btcAmountModifier = sharedAmountModifier,
                            )
                        }

                        DraftTxStatus.Sent -> {
                            TransactionSuccess(
                                modifier = Modifier.fillMaxSize(),
                                amountInSats = state.transaction.amountSats.toLong(),
                                receiver = state.profileLightningAddress
                                    ?: state.transaction.targetLud16
                                    ?: state.transaction.targetLnUrl?.ellipsizeLnUrl()
                                    ?: state.transaction.lnInvoiceDescription
                                    ?: state.transaction.targetOnChainAddress?.ellipsizeOnChainAddress(),
                                onDoneClick = onClose,
                            )
                        }

                        DraftTxStatus.Failed -> {
                            TransactionFailed(
                                modifier = Modifier
                                    .background(color = AppTheme.colorScheme.surfaceVariant)
                                    .fillMaxSize(),
                                errorMessage = state.error?.message
                                    ?: stringResource(id = R.string.app_generic_error),
                                onCloseClick = onClose,
                            )
                        }
                    }
                }
            }
        },
    )
}

@ExperimentalMaterial3Api
@Composable
private fun CreateTransactionTopAppBar(txStatus: DraftTxStatus, onClose: () -> Unit) {
    if (txStatus != DraftTxStatus.Sent) {
        PrimalTopAppBar(
            title = when (txStatus) {
                DraftTxStatus.Draft -> stringResource(id = R.string.wallet_create_transaction_draft_title)
                DraftTxStatus.Sending -> stringResource(
                    id = R.string.wallet_create_transaction_sending_title,
                )

                DraftTxStatus.Failed -> stringResource(id = R.string.wallet_create_transaction_failed_title)
                DraftTxStatus.Sent -> stringResource(id = R.string.wallet_create_transaction_success_title)
            },
            navigationIcon = PrimalIcons.ArrowBack,
            navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
            showDivider = false,
            onNavigationIconClick = onClose,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppTheme.colorScheme.surfaceVariant,
                scrolledContainerColor = AppTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

fun String.ellipsizeLnUrl() = this.ellipsizeMiddle(size = 8).lowercase()

fun String.ellipsizeOnChainAddress() = this.ellipsizeMiddle(size = 16)
