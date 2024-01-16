package net.primal.android.wallet.transactions.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.PrimalTheme
import net.primal.android.wallet.transactions.details.TransactionDetailsContract.UiState
import net.primal.android.wallet.transactions.list.TransactionDataUi

@Composable
fun TransactionDetailsScreen(viewModel: TransactionDetailsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    TransactionDetailsScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(state: UiState, onClose: () -> Unit) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = "Calculating...",
                navigationIcon = PrimalIcons.ArrowBack,
                showDivider = false,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(state = scrollState),
            ) {
            }
        },
    )
}

class TransactionParameterProvider : PreviewParameterProvider<TransactionDataUi> {
    override val values: Sequence<TransactionDataUi>
        get() = sequenceOf()
}

@Preview
@Composable
fun PreviewTransactionDetail(
    @PreviewParameter(provider = TransactionParameterProvider::class)
    parameter: TransactionDataUi,
) {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            TransactionDetailsScreen(
                state = UiState(),
                onClose = {},
            )
        }
    }
}
