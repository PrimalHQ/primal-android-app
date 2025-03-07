package net.primal.networking.model.ext

import net.primal.networking.model.NostrEventKind
import net.primal.networking.model.NostrEventKindRange

fun NostrEventKind.isPrimalEventKind() = value in NostrEventKindRange.PrimalEvents

fun NostrEventKind.isNotPrimalEventKind() = !isPrimalEventKind()

fun NostrEventKind.isUnknown() = this == NostrEventKind.Unknown

fun NostrEventKind.isNotUnknown() = !isUnknown()
