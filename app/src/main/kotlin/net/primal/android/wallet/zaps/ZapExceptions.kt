package net.primal.android.wallet.zaps

data class InvalidZapRequestException(override val cause: Throwable? = null) : IllegalArgumentException()

data class ZapFailureException(override val cause: Throwable) : RuntimeException()
