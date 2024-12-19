package net.primal.android.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun verifyBiometricIdentity(
    activity: Context,
    biometricPromptParams: BiometricPromptParams,
    onAuthSucceed: () -> Unit,
    onAuthFailed: (() -> Unit)? = null,
    onAuthError: ((errorCode: Int, errString: CharSequence) -> Unit)? = null,
) {
    if (isBiometricAvailable(activity)) {
        showBiometricPrompt(
            activity = activity,
            params = biometricPromptParams,
            onAuthSucceed = onAuthSucceed,
            onAuthFailed = onAuthFailed,
            onAuthError = onAuthError,
        )
    } else {
        onAuthSucceed()
    }
}

private fun isBiometricAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (
        biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )
    ) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }
}

private fun showBiometricPrompt(
    activity: Context,
    params: BiometricPromptParams,
    onAuthSucceed: () -> Unit,
    onAuthFailed: (() -> Unit)?,
    onAuthError: ((errorCode: Int, errString: CharSequence) -> Unit)?,
) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(params.title)
        .setSubtitle(params.subtitle)
        .setDescription(params.description)
        .setConfirmationRequired(false)
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )
        .build()

    val biometricPrompt =
        BiometricPrompt(
            activity as FragmentActivity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthSucceed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onAuthError?.invoke(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    onAuthFailed?.invoke()
                }
            },
        )

    biometricPrompt.authenticate(promptInfo)
}

data class BiometricPromptParams(
    val title: String,
    val subtitle: String?,
    val description: String?,
)
