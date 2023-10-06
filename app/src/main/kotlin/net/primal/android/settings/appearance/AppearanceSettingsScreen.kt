package net.primal.android.settings.appearance

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.PrimalTheme

@Composable
fun AppearanceSettingsScreen(
    viewModel: AppearanceSettingsViewModel,
    onClose: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedErrorHandler(viewModel = viewModel)

    AppearanceSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    state: AppearanceSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (AppearanceSettingsContract.UiEvent) -> Unit
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = "Appearance",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {

            }
        }
    )
}

@Composable
fun LaunchedErrorHandler(
    viewModel: AppearanceSettingsViewModel
) {
    val genericMessage = stringResource(id = R.string.app_generic_error)
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.state.filter { it.error != null }.map { it.error }.filterNotNull().collect {
            uiScope.launch {
                Toast.makeText(
                    context, genericMessage, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@Preview
@Composable
fun PreviewAppearanceSettingsScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        AppearanceSettingsScreen(
            state = AppearanceSettingsContract.UiState(selectedThemeName = PrimalTheme.Sunset.themeName),
            onClose = {},
            eventPublisher = {}
        )
    }
}