package net.primal.networking.relays

val FALLBACK_RELAYS = listOf(
    "wss://relay.primal.net",
    "wss://relay.damus.io",
    "wss://relay.nostr.band",
    "wss://relay.current.fyi",
    "wss://purplepag.es",
    "wss://nos.lol",
    "wss://offchain.pub",
    "wss://nostr.bitcoiner.social",
).map { it.toRelay() }
