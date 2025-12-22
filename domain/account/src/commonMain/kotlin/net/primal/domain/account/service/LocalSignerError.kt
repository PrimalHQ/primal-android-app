package net.primal.domain.account.service

sealed class LocalSignerError(override val message: String) : RuntimeException(message) {
    data object UserApprovalRequired : LocalSignerError("User approval required for this request.")
    data object AutoDenied : LocalSignerError("Request denied by already saved permission.")
}
