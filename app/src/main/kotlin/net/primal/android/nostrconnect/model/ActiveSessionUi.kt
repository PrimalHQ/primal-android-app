package net.primal.android.nostrconnect.model

import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.domain.account.model.AppSession

data class ActiveSessionUi(
    val sessionId: String,
    val connectionId: String,
    val appName: String?,
    val appUrl: String?,
    val appImageUrl: String?,
    val userAccount: UserAccountUi?,
)

fun AppSession.asUi(userAccount: UserAccountUi?) =
    ActiveSessionUi(
        sessionId = sessionId,
        connectionId = connectionId,
        appName = name,
        appUrl = url,
        appImageUrl = image,
        userAccount = userAccount,
    )
