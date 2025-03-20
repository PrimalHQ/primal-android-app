package net.primal.android.core.serialization.json

import kotlinx.serialization.json.Json
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.RelayPermission

val NostrJsonEncodeDefaults = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

val NostrNotaryJson = Json {
    ignoreUnknownKeys = true
}

fun List<Relay>.toNostrRelayMap(): Map<String, RelayPermission> {
    val map = mutableMapOf<String, RelayPermission>()
    this.forEach { map[it.url] = RelayPermission(read = it.read, write = it.write) }
    return map
}
