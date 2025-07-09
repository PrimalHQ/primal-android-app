package net.primal.android.auth.welcome

interface WelcomeContract {
    data class ScreenCallbacks(
        val onSignInClick: () -> Unit,
        val onCreateAccountClick: () -> Unit,
        val onRedeemCodeClick: () -> Unit,
    )
}
