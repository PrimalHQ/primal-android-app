package net.primal.android.navigation

interface SplashContract {

    sealed class SideEffect {
        object NoActiveAccount : SideEffect()
        data class ActiveAccount(val userPubkey: String) : SideEffect()
    }

}