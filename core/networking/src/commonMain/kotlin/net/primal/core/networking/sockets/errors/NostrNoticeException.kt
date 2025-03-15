package net.primal.core.networking.sockets.errors

class NostrNoticeException(val reason: String?, val subscriptionId: String? = null) : RuntimeException()
