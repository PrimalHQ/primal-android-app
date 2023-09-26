package net.primal.android.user.accounts

import net.primal.android.user.domain.toRelay

val BOOTSTRAP_RELAYS = listOf(
    "wss://relay.damus.io",
    "wss://eden.nostr.land",
    "wss://nos.lol",
    "wss://relay.snort.social",
    "wss://relay.current.fyi",
    "wss://brb.io",
    "wss://nostr.orangepill.dev",
    "wss://nostr-pub.wellorder.net",
    "wss://nostr.wine",
    "wss://nostr.bitcoiner.social",
    "wss://relay.primal.net",
).map { it.toRelay() }
