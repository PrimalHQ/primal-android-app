package net.primal.android.navigation.splash

interface SplashContract {

    sealed class SideEffect {
        data object NoActiveAccount : SideEffect()
        data class ActiveAccount(val userPubkey: String) : SideEffect()
    }
}
