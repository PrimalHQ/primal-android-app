package net.primal.android.settings.connected.permissions.remote

import net.primal.android.core.errors.UiError
import net.primal.android.settings.connected.model.PermissionGroupUi
import net.primal.domain.account.model.AppPermissionAction

interface RemoteAppPermissionsContract {
    data class UiState(
        val appName: String? = null,
        val appIconUrl: String? = null,
        val appLastSessionAt: Long? = null,
        val loading: Boolean = true,
        val permissions: List<PermissionGroupUi> = emptyList(),
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class UpdatePermission(
            val permissionIds: List<String>,
            val action: AppPermissionAction,
        ) : UiEvent()
        data object Retry : UiEvent()
        data object DismissError : UiEvent()
        data object ResetPermissions : UiEvent()
    }
}
