package net.primal.android.networking.sockets

import net.primal.android.nostr.model.NostrEventKind

fun SocketQueryResult.findNostrEvent(kind: NostrEventKind) =
    nostrEvents.find { kind.value == it.kind }

fun SocketQueryResult.findPrimalEvent(kind: NostrEventKind) =
    primalEvents.find { kind.value == it.kind }

fun SocketQueryResult.filterNostrEvents(kind: NostrEventKind) =
    nostrEvents.filter { kind.value == it.kind }

fun SocketQueryResult.filterPrimalEvents(kind: NostrEventKind) =
    primalEvents.filter { kind.value == it.kind }
