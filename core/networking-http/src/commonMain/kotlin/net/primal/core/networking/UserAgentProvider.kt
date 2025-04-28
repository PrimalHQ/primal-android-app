package net.primal.core.networking

import net.primal.core.utils.AppBuildHelper

object UserAgentProvider {
    fun resolveUserAgent() = "${AppBuildHelper.getAppName()}/${AppBuildHelper.getAppVersion()}"
}
