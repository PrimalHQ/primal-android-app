package net.primal.android.networking

import net.primal.android.BuildConfig

object UserAgentProvider {

    const val APP_NAME = "Primal-Android"

    const val USER_AGENT = "$APP_NAME/${BuildConfig.VERSION_NAME}"
}
