package net.primal.android.networking.relays

import net.primal.android.user.domain.toRelay

val FALLBACK_RELAY_URLS = listOf(
    "wss://relay.primal.net",
    "wss://relay.damus.io",
    "wss://relay.nostr.band",
    "wss://relay.current.fyi",
    "wss://purplepag.es",
    "wss://nos.lol",
    "wss://offchain.pub",
    "wss://nostr.bitcoiner.social",
)

val FALLBACK_RELAYS = FALLBACK_RELAY_URLS.map { it.toRelay() }
