package net.primal.android.user.domain

import net.primal.android.user.db.Relay as RelayPO

fun Relay.mapToRelayPO(userId: String, kind: RelayKind) =
    RelayPO(
        userId = userId,
        kind = kind,
        url = this.url.cleanWebSocketUrl(),
        read = this.read,
        write = this.write,
    )

fun RelayPO.mapToRelayDO() =
    Relay(
        url = this.url,
        read = this.read,
        write = this.write,
    )

fun String.cleanWebSocketUrl(): String {
    return replace("https://", "wss://", ignoreCase = true)
        .replace("http://", "ws://", ignoreCase = true)
        .let { if (it.endsWith("/")) it.dropLast(1) else it }
}
