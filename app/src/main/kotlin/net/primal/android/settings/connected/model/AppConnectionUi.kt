package net.primal.android.settings.connected.model

import net.primal.android.user.domain.UserAccount
import net.primal.domain.account.model.AppConnection
import net.primal.domain.links.CdnImage

data class AppConnectionUi(
    val clientPubKey: String,
    val appName: String,
    val appImage: CdnImage?,
    val userAvatarCdnImage: CdnImage?,
)

fun AppConnection.asAppConnectionUi(userAccount: UserAccount?): AppConnectionUi {
    return AppConnectionUi(
        clientPubKey = this.clientPubKey,
        appName = this.name ?: "Unknown App",
        appImage = this.image?.let { CdnImage(it) },
        userAvatarCdnImage = userAccount?.avatarCdnImage,
    )
}
