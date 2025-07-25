package net.primal.android.premium.buying.name

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.core.compose.PrimalOutlinedTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.buying.home.PRO_ORANGE
import net.primal.android.premium.ui.PremiumPrimalNameTable
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.store.domain.SubscriptionTier

@ExperimentalMaterial3Api
@Composable
fun PremiumPrimalNameStage(
    titleText: String,
    onBack: () -> Unit,
    onPrimalNameAvailable: (String) -> Unit,
    initialName: String? = null,
    subscriptionTier: SubscriptionTier = SubscriptionTier.PREMIUM,
    snackbarHostState: SnackbarHostState? = null,
) {
    val viewModel = hiltViewModel<PremiumPrimalNameViewModel>()
    val uiState = viewModel.state.collectAsState()

    PremiumPrimalNameStage(
        titleText = titleText,
        subscriptionTier = subscriptionTier,
        onBack = onBack,
        onPrimalNameAvailable = onPrimalNameAvailable,
        eventPublisher = viewModel::setEvent,
        initialName = initialName,
        state = uiState.value,
        snackbarHostState = snackbarHostState,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun PremiumPrimalNameStage(
    titleText: String,
    subscriptionTier: SubscriptionTier,
    onBack: () -> Unit,
    state: PremiumPrimalNameContract.UiState,
    onPrimalNameAvailable: (String) -> Unit,
    eventPublisher: (PremiumPrimalNameContract.UiEvent) -> Unit,
    initialName: String? = null,
    snackbarHostState: SnackbarHostState? = null,
) {
    var primalName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        val name = initialName ?: ""
        mutableStateOf(TextFieldValue(text = name, selection = TextRange(start = name.length, end = name.length)))
    }

    LaunchedEffect(state.isNameAvailable) {
        if (state.isNameAvailable == true) {
            onPrimalNameAvailable(primalName.text)
            eventPublisher(PremiumPrimalNameContract.UiEvent.ResetNameAvailable)
        }
    }

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = titleText,
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onBack,
                showDivider = false,
            )
        },
        bottomBar = {
            PrimalNameSearchButton(
                subscriptionTier = subscriptionTier,
                onClick = { eventPublisher(PremiumPrimalNameContract.UiEvent.CheckPrimalName(primalName.text)) },
            )
        },
        snackbarHost = {
            snackbarHostState?.let {
                SnackbarHost(hostState = it)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrimalOutlinedTextField(
                header = null,
                value = primalName,
                onValueChange = {
                    eventPublisher(PremiumPrimalNameContract.UiEvent.ResetNameAvailable)
                    primalName = it
                },
                forceFocus = true,
                textAlign = TextAlign.Center,
                isError = state.isNameAvailable == false,
                fontSize = 20.sp,
            )
            Text(
                text = if (state.isNameAvailable == false) {
                    stringResource(id = R.string.premium_primal_name_unavailable_message)
                } else {
                    " "
                },
                color = AppTheme.colorScheme.error,
                style = AppTheme.typography.bodyMedium,
            )

            PremiumPrimalNameTable(
                modifier = Modifier.padding(horizontal = 24.dp),
                primalName = primalName.text,
            )
        }
    }
}

@Composable
private fun PrimalNameSearchButton(subscriptionTier: SubscriptionTier, onClick: () -> Unit) {
    PrimalFilledButton(
        modifier = Modifier
            .imePadding()
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(32.dp),
        onClick = onClick,
        containerColor = if (subscriptionTier.isPremiumTier()) {
            AppTheme.colorScheme.primary
        } else {
            PRO_ORANGE
        },
    ) {
        Text(
            text = stringResource(id = R.string.premium_primal_name_search_button),
            style = AppTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
