package net.primal.android.settings.connected.permissions

import net.primal.android.core.errors.UiError
import net.primal.android.settings.connected.model.PermissionUi
import net.primal.domain.account.model.PermissionAction

interface AppPermissionsContract {
    data class UiState(
        val appName: String? = null,
        val appIconUrl: String? = null,
        val appLastSessionAt: Long? = null,
        val loading: Boolean = true,
        val permissions: List<PermissionUi> = emptyList(),
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class ChangePermission(val permissionId: String, val action: PermissionAction) : UiEvent()
        data object DismissError : UiEvent()
    }
}
