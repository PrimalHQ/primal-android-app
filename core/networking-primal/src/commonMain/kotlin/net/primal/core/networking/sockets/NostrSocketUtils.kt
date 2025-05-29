package net.primal.core.networking.sockets

import kotlin.uuid.Uuid
import net.primal.core.utils.AppBuildHelper

fun Uuid.toPrimalSubscriptionId(): String = "${AppBuildHelper.getPlatformName()}-$this".lowercase()
