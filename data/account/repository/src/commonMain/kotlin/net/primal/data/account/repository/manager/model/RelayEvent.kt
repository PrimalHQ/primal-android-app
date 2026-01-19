package net.primal.data.account.repository.manager.model

sealed class RelayEvent(
    open val relayUrl: String,
) {
    data class Connected(override val relayUrl: String) : RelayEvent(relayUrl = relayUrl)
    data class Disconnected(override val relayUrl: String) : RelayEvent(relayUrl = relayUrl)
}
