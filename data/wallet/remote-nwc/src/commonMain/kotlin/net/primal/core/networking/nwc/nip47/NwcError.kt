package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.Serializable

@Serializable
data class NwcError(
    val code: String,
    val message: String,
) {
    companion object {
        const val RATE_LIMITED = "RATE_LIMITED"
        const val NOT_IMPLEMENTED = "NOT_IMPLEMENTED"
        const val INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE"
        const val QUOTA_EXCEEDED = "QUOTA_EXCEEDED"
        const val RESTRICTED = "RESTRICTED"
        const val UNAUTHORIZED = "UNAUTHORIZED"
        const val INTERNAL = "INTERNAL"
        const val OTHER = "OTHER"
    }
}
