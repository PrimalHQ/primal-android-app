package net.primal.networking.sockets.errors

class NostrNoticeException(val reason: String?, val subscriptionId: String? = null) : RuntimeException()
