package net.primal.android.settings.connected.model

import net.primal.android.user.domain.UserAccount
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.RemoteAppConnection
import net.primal.domain.links.CdnImage

data class AppConnectionUi(
    val connectionId: String,
    val appId: String,
    val userAvatarCdnImage: CdnImage?,
    val appName: String? = null,
    val appIconUrl: String? = null,
    val isLocal: Boolean = false,
)

fun RemoteAppConnection.asAppConnectionUi(userAccount: UserAccount?): AppConnectionUi {
    return AppConnectionUi(
        connectionId = this.clientPubKey,
        appId = this.clientPubKey,
        appName = this.name,
        userAvatarCdnImage = userAccount?.avatarCdnImage,
        appIconUrl = this.image,
        isLocal = false,
    )
}

fun LocalApp.asAppConnectionUi(userAccount: UserAccount?): AppConnectionUi {
    return AppConnectionUi(
        connectionId = this.identifier,
        appId = this.packageName,
        appName = this.name,
        userAvatarCdnImage = userAccount?.avatarCdnImage,
        appIconUrl = null,
        isLocal = true,
    )
}
