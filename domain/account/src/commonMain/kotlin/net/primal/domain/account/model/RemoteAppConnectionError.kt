package net.primal.domain.account.model

sealed class RemoteAppConnectionError {
    data class AllRelaysFailed(val message: String) : RemoteAppConnectionError()
    data class NetworkUnavailable(val message: String) : RemoteAppConnectionError()
}
