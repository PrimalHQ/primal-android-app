package net.primal.data.account.repository.service

sealed class LocalSignerError(override val message: String) : Throwable(message = message) {
    data class UserApprovalRequired(override val message: String = "User approval required for this request.") :
        LocalSignerError(message = message)

    data class AutoDenied(override val message: String = "Request denied by already saved permission.") :
        LocalSignerError(message = message)

    data class AppNotFound(override val message: String = "App not found. User might have deleted it.") :
        LocalSignerError(message = message)
}
