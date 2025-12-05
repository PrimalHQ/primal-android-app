package net.primal.android.nostrconnect.model

import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.domain.account.model.AppSession

data class ActiveSessionUi(
    val sessionId: String,
    val clientPubKey: String,
    val appName: String?,
    val appUrl: String?,
    val appImageUrl: String?,
    val userAccount: UserAccountUi?,
)

fun AppSession.asUi(userAccount: UserAccountUi?) =
    ActiveSessionUi(
        sessionId = sessionId,
        clientPubKey = clientPubKey,
        appName = name,
        appUrl = url,
        appImageUrl = image,
        userAccount = userAccount,
    )
