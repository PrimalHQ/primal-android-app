package net.primal.data.account.repository.service

sealed class LocalSignerError(override val message: String) : RuntimeException(message) {
    data object UserApprovalRequired : LocalSignerError("User approval required for this request.") {
        private fun readResolve(): Any = UserApprovalRequired
    }

    data object AutoDenied : LocalSignerError("Request denied by already saved permission.") {
        private fun readResolve(): Any = AutoDenied
    }

    data object AppNotFound : LocalSignerError("App not found. User might have deleted it.") {
        private fun readResolve(): Any = AppNotFound
    }
}
