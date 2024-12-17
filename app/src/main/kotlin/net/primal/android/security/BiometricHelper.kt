package net.primal.android.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun biometricAuthentication(
        activity: Context,
        onAuthSucceed: (BiometricPrompt.AuthenticationResult) -> Unit,
        biometricPromptParams: BiometricPromptParams,
    ) {
        showBiometricPrompt(
            activity = activity,
            onAuthSucceed = onAuthSucceed,
            params = biometricPromptParams,
        )
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK,
            )
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    private fun showBiometricPrompt(
        activity: Context,
        onAuthSucceed: (BiometricPrompt.AuthenticationResult) -> Unit,
        params: BiometricPromptParams,
    ) {
        val promptInfo = getPromptInfo(
            title = params.title,
            subtitle = params.subtitle,
            description = params.description,
            cancelButtonText = params.cancelButtonText,
        )

        val biometricPrompt = getBiometricPrompt(context = activity as FragmentActivity, onAuthSucceed = onAuthSucceed)

        biometricPrompt.authenticate(promptInfo)
    }

    private fun getPromptInfo(
        title: String,
        subtitle: String,
        description: String,
        cancelButtonText: String,
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(cancelButtonText)
            .setConfirmationRequired(false)
            .build()
    }

    private fun getBiometricPrompt(
        context: FragmentActivity,
        onAuthSucceed: (BiometricPrompt.AuthenticationResult) -> Unit,
    ): BiometricPrompt {
        val biometricPrompt =
            BiometricPrompt(
                context,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onAuthSucceed(result)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                },
            )
        return biometricPrompt
    }
}

data class BiometricPromptParams(
    val title: String,
    val subtitle: String,
    val description: String,
    val cancelButtonText: String,
)
