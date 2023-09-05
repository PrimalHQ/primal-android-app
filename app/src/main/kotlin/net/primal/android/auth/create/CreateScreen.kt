package net.primal.android.auth.create

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.PrimalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    state: CreateContract.UiState,
    eventPublisher: (CreateContract.UiEvent) -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = "New Account",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            CreateContent(state = state, paddingValues = paddingValues)
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateContent(
    state: CreateContract.UiState,
    paddingValues: PaddingValues
) {

}

@Preview
@Composable
fun PreviewCreateScreen() {
    PrimalTheme {
        CreateScreen(state = CreateContract.UiState(), eventPublisher = {}, onClose = {})
    }
}
