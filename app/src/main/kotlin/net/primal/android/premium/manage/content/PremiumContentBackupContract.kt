package net.primal.android.premium.manage.content

interface PremiumContentBackupContract {

    data class UiState(
        val broadcasting: Boolean = false,
    )

    sealed class UiEvent

    sealed class SideEffect
}
