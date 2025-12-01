package net.primal.android.settings.connected.permissions

import net.primal.android.core.errors.UiError
import net.primal.android.settings.connected.model.PermissionGroupUi
import net.primal.domain.account.model.PermissionAction

interface AppPermissionsContract {
    data class UiState(
        val appName: String? = null,
        val appIconUrl: String? = null,
        val appLastSessionAt: Long? = null,
        val loading: Boolean = true,
        val permissions: List<PermissionGroupUi> = emptyList(),
        val error: UiError? = null,
        val confirmingReset: Boolean = false,
    )

    sealed class UiEvent {
        data class UpdatePermission(
            val permissionIds: List<String>,
            val action: PermissionAction,
        ) : UiEvent()
        data object Retry : UiEvent()
        data object DismissError : UiEvent()
        data object RequestResetPermissions : UiEvent()
        data object ConfirmResetPermissions : UiEvent()
        data object DismissResetConfirmation : UiEvent()
    }
}
