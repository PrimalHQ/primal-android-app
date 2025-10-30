package net.primal.android.settings.connected.model

import net.primal.android.user.domain.UserAccount
import net.primal.domain.account.model.AppConnection
import net.primal.domain.links.CdnImage

data class AppConnectionUi(
    val connectionId: String,
    val appName: String,
    val appImage: CdnImage?,
    val userAvatarCdnImage: CdnImage?,
    val isActive: Boolean,
)

fun AppConnection.asAppConnectionUi(userAccount: UserAccount?, isActive: Boolean): AppConnectionUi {
    return AppConnectionUi(
        connectionId = this.connectionId,
        appName = this.name ?: "Unknown App",
        appImage = this.image?.let { CdnImage(it) },
        userAvatarCdnImage = userAccount?.avatarCdnImage,
        isActive = isActive,
    )
}
