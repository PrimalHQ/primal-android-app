package net.primal.android.nostr.ext

import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.NostrKindEventRange


fun NostrEventKind.isPrimalEventKind() = value in NostrKindEventRange.PrimalEvents

fun NostrEventKind.isNotPrimalEventKind() = !isPrimalEventKind()

fun NostrEventKind.isUnknown() = this == NostrEventKind.Unknown

fun NostrEventKind.isNotUnknown() = !isUnknown()
