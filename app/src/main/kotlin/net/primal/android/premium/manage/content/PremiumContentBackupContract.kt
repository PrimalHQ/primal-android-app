package net.primal.android.premium.manage.content

import net.primal.android.premium.manage.content.model.ContentType

interface PremiumContentBackupContract {

    data class UiState(
        val broadcasting: Boolean = false,
        val contentTypes: List<ContentType> = emptyList(),
        val allEventsCount: Long? = null,
    )

    sealed class UiEvent

    sealed class SideEffect
}
