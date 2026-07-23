package net.primal.android.networking.relays

import net.primal.android.user.domain.toRelay

val FALLBACK_RELAY_URLS = listOf(
    "wss://relay.primal.net",
    "wss://nos.lol",
    "wss://relay.nostr.net",
    "wss://relay.snort.social",
    "wss://purplepag.es",
    "wss://nostr.land",
)

val FALLBACK_RELAYS = FALLBACK_RELAY_URLS.map { it.toRelay() }
