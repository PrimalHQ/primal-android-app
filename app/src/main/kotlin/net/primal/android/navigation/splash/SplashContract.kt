package net.primal.android.navigation.splash

interface SplashContract {

    sealed class SideEffect {
        object NoActiveAccount : SideEffect()
        data class ActiveAccount(val userPubkey: String) : SideEffect()
    }

}
