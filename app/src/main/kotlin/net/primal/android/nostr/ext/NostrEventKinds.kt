package net.primal.android.nostr.ext

import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrEventKindRange

fun NostrEventKind.isPrimalEventKind() = value in NostrEventKindRange.PrimalEvents

fun NostrEventKind.isNotPrimalEventKind() = !isPrimalEventKind()

fun NostrEventKind.isUnknown() = this == NostrEventKind.Unknown

fun NostrEventKind.isNotUnknown() = !isUnknown()
