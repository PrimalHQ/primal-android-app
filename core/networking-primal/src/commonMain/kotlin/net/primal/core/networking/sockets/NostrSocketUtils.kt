package net.primal.core.networking.sockets

import kotlin.uuid.Uuid
import net.primal.core.utils.createAppBuildHelper

private val appBuildHelper = createAppBuildHelper()

fun Uuid.toPrimalSubscriptionId(): String = "${appBuildHelper.getPlatformName()}-$this".lowercase()
