package net.primal.android.premium.manage.content

import net.primal.android.premium.manage.content.model.ContentGroup
import net.primal.android.premium.manage.content.model.ContentType

interface PremiumContentBackupContract {

    data class UiState(
        val anyBroadcasting: Boolean = false,
        val contentTypes: List<ContentType> = ContentGroup.entries.map { ContentType(group = it) },
    )

    sealed class UiEvent {
        data object StartBroadcastingMonitor : UiEvent()
        data object StopBroadcastingMonitor : UiEvent()
        data class StartBroadcasting(val type: ContentType) : UiEvent()
        data object StopBroadcasting : UiEvent()
    }
}
