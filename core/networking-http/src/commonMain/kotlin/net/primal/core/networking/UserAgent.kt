package net.primal.core.networking

import net.primal.core.utils.AppBuildHelper

internal fun resolveUserAgent() = "${AppBuildHelper.getAppName()}/${AppBuildHelper.getAppVersion()}"
