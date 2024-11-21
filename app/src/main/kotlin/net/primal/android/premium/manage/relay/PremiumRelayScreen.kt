package net.primal.android.premium.manage.relay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

@Composable
fun PremiumRelayScreen(viewModel: PremiumRelayViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    PremiumRelayScreen(
        onClose = onClose,
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumRelayScreen(
    onClose: () -> Unit,
    state: PremiumRelayContract.UiState,
    eventPublisher: (PremiumRelayContract.UiEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { stringResource(id = R.string.app_generic_error) },
        onErrorDismiss = { eventPublisher(PremiumRelayContract.UiEvent.DismissError) },
    )
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_relay_top_app_bar_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .padding(36.dp)
                    .navigationBarsPadding()
                    .fillMaxWidth(),
            ) {
                val text = when (state.isConnected) {
                    true -> stringResource(id = R.string.premium_relay_connected_button)
                    false -> stringResource(id = R.string.premium_relay_connect_button)
                }
                PrimalLoadingButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isConnected,
                    onClick = { eventPublisher(PremiumRelayContract.UiEvent.ConnectToRelay) },
                    text = text,
                    loading = state.addingRelay,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 36.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
        ) {
            RelayHeader(version = state.version)
            PrimalDivider(modifier = Modifier.width(72.dp))
            RelayDescription(modifier = Modifier.padding(horizontal = 8.dp))
            RelayField(isConnected = state.isConnected, relayUrl = state.relayUrl)
        }
    }
}

@Composable
private fun RelayHeader(modifier: Modifier = Modifier, version: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.primal_wave_logo_summer),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.premium_relay_title),
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
        )
        Text(
            text = stringResource(id = R.string.premium_relay_running) + " $version",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

@Composable
private fun RelayField(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    relayUrl: String,
) {
    Box(
        modifier = modifier
            .clip(AppTheme.shapes.medium)
            .fillMaxWidth()
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val indicatorColor = when (isConnected) {
                true -> AppTheme.extraColorScheme.reposted
                false -> AppTheme.extraColorScheme.onSurfaceVariantAlt4
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(indicatorColor),
            )
            Text(
                text = relayUrl,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun RelayDescription(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(id = R.string.premium_relay_description),
        style = AppTheme.typography.bodyLarge,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        textAlign = TextAlign.Center,
        fontSize = 16.sp,
    )
}
