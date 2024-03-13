package net.primal.android.auth.onboarding.wallet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.compose.onboardingTextHintTypography
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.wallet.activation.WalletActivationContract
import net.primal.android.wallet.activation.WalletActivationViewModel

@Composable
fun OnboardingWalletActivation(viewModel: WalletActivationViewModel, onDoneOrDismiss: () -> Unit) {
    val uiState = viewModel.uiState.collectAsState()
    OnboardingWalletActivation(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onDoneOrDismiss = onDoneOrDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWalletActivation(
    state: WalletActivationContract.UiState,
    eventPublisher: (WalletActivationContract.UiEvent) -> Unit,
    onDoneOrDismiss: () -> Unit,
) {
    BackHandler {}
    ColumnWithBackground(
        backgroundPainter = painterResource(id = R.drawable.onboarding_spot5),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PrimalTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                    showDivider = false,
                    textColor = Color.White,
                    title = stringResource(id = R.string.wallet_activation_title),
                )
            },
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues)) {
                    state.status
                    eventPublisher.toString()
                }
            },
            bottomBar = {
                OnboardingBottomBar(
                    buttonText = "Next",
                    onButtonClick = { },
                    footer = {
                        TextButton(
                            modifier = Modifier.height(56.dp),
                            onClick = onDoneOrDismiss,
                        ) {
                            Text(
                                text = stringResource(id = R.string.onboarding_button_label_i_will_do_this_later),
                                style = onboardingTextHintTypography(),
                            )
                        }
                    },
                )
            },
        )
    }
}
