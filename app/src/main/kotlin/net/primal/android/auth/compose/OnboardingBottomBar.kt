package net.primal.android.auth.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingBottomBar(
    buttonText: String,
    onButtonClick: () -> Unit,
    buttonEnabled: Boolean = true,
    buttonLoading: Boolean = false,
    footer: @Composable ColumnScope.() -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingButton(
            text = buttonText,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(alignment = Alignment.CenterHorizontally),
            onClick = {
                keyboardController?.hide()
                onButtonClick()
            },
            enabled = buttonEnabled,
            loading = buttonLoading,
        )

        footer()
    }
}
