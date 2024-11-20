package net.primal.android.premium.manage.contact

import net.primal.android.premium.manage.contact.model.FollowListBackup

interface PremiumContactListContract {

    data class UiState(
        val fetching: Boolean = false,
        val backups: List<FollowListBackup> = emptyList(),
    )

    sealed class UiEvent {
        data class RecoverFollowList(val backup: FollowListBackup) : UiEvent()
    }
}
