package net.primal.android.premium.manage.contact

interface PremiumContactListContract {

    data class UiState(
        val fetching: Boolean = false,
    )

    sealed class UiEvent
}
