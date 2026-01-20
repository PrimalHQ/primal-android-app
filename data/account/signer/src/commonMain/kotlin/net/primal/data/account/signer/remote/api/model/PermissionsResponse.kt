package net.primal.data.account.signer.remote.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PermissionsResponse(
    val permissions: List<PermissionInfo>,
    val groups: List<GroupInfo>,
)

@Serializable
data class PermissionInfo(
    val id: String,
    val title: String,
)

@Serializable
data class GroupInfo(
    val id: String,
    val title: String,
    @SerialName("permissions") val permissionIds: List<String>,
)
