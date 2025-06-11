package net.primal.core.networking

import net.primal.core.utils.createAppBuildHelper

object UserAgentProvider {

    private val appBuildHelper = createAppBuildHelper()

    fun resolveUserAgent() = "${appBuildHelper.getAppName()}/${appBuildHelper.getAppVersion()}"
}
