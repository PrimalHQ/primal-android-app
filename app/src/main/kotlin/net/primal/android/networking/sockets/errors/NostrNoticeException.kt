package net.primal.android.networking.sockets.errors

import java.util.*

class NostrNoticeException(val reason: String?, val subscriptionId: UUID? = null) : RuntimeException()
