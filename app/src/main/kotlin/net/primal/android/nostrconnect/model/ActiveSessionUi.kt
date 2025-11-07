package net.primal.android.nostrconnect.model

import net.primal.android.drawer.multiaccount.model.UserAccountUi

data class ActiveSessionUi(
    val sessionId: String,
    val connectionId: String,
    val appName: String?,
    val appUrl: String?,
    val appImageUrl: String?,
    val userAccount: UserAccountUi,
)
