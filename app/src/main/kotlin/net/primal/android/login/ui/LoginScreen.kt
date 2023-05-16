package net.primal.android.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.LoadingElevatedButton
import net.primal.android.login.LoginContract
import net.primal.android.login.LoginViewModel
import net.primal.android.theme.PrimalTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
) {
    val uiState = viewModel.state.collectAsState()

    LoginScreen(
        state = uiState.value,
    )
}

@Composable
fun LoginScreen(
    state: LoginContract.UiState,
) {

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    modifier = Modifier.padding(all = 16.dp).fillMaxWidth(),
                    text = "Paste your nsec key to login to Nostr",
                    textAlign = TextAlign.Center
                )

                var nsecState by remember { mutableStateOf("") }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = nsecState,
                    onValueChange = { input -> nsecState = input },
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.login_nsec)) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )

                LoadingElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.8f)
                        .padding(vertical = 16.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    onClick = {},
                    content = {
                        Text(text = "Login")
                    }
                )
            }
        }
    )

}


@Preview
@Composable
fun PreviewLoginScreen() {
    PrimalTheme {
        LoginScreen(state = LoginContract.UiState())
    }
}