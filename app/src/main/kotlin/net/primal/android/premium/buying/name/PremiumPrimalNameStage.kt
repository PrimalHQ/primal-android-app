package net.primal.android.premium.buying.name

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import net.primal.android.premium.ui.PremiumPrimalNameTable
import net.primal.android.theme.AppTheme

@Composable
fun PremiumPrimalNameStage(
    titleText: String,
    onBack: () -> Unit,
    onPrimalNameAvailable: (String) -> Unit,
    initialName: String? = null,
) {
    val viewModel = hiltViewModel<PremiumPrimalNameViewModel>()
    val uiState = viewModel.state.collectAsState()

    PremiumPrimalNameStage(
        titleText = titleText,
        onBack = onBack,
        onPrimalNameAvailable = onPrimalNameAvailable,
        eventPublisher = viewModel::setEvent,
        initialName = initialName,
        state = uiState.value,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumPrimalNameStage(
    titleText: String,
    onBack: () -> Unit,
    state: PremiumPrimalNameContract.UiState,
    onPrimalNameAvailable: (String) -> Unit,
    eventPublisher: (PremiumPrimalNameContract.UiEvent) -> Unit,
    initialName: String? = null,
) {
    var primalName by remember { mutableStateOf(initialName ?: "") }

    LaunchedEffect(state.isNameAvailable) {
        if (state.isNameAvailable == true) {
            onPrimalNameAvailable(primalName)
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
            PrimalFilledButton(
                modifier = Modifier
                    .imePadding()
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(32.dp),
                onClick = {
                    eventPublisher(PremiumPrimalNameContract.UiEvent.CheckPrimalName(primalName))
                },
            ) {
                Text(
                    text = stringResource(id = R.string.premium_primal_name_search_button),
                    style = AppTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
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
                    primalName = it.trim()
                },
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
                primalName = primalName,
            )
        }
    }
}
