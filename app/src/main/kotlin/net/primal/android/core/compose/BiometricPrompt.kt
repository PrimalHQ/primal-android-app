package net.primal.android.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.security.BiometricPromptParams
import net.primal.android.security.verifyBiometricIdentity

@Composable
fun BiometricPrompt(
    onAuthSuccess: () -> Unit,
    onAuthDismiss: () -> Unit,
    title: String = stringResource(id = R.string.biometric_prompt_title),
    subtitle: String? = null,
    description: String? = stringResource(id = R.string.biometric_prompt_description),
    onAuthError: ((errorCode: Int, errString: CharSequence) -> Unit)? = null,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        verifyBiometricIdentity(
            activity = context,
            onAuthSucceed = {
                onAuthDismiss()
                onAuthSuccess()
            },
            onAuthError = { errorCode, errorMessage ->
                onAuthDismiss()
                onAuthError?.invoke(errorCode, errorMessage)
            },
            biometricPromptParams = BiometricPromptParams(
                title = title,
                subtitle = subtitle,
                description = description,
            ),
        )
    }
}
