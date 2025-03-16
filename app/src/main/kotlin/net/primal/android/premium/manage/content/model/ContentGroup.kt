package net.primal.android.premium.manage.content.model

import net.primal.domain.nostr.NostrEventKind

enum class ContentGroup(val kinds: List<Int>?) {
    Notes(kinds = listOf(NostrEventKind.ShortTextNote.value)),
    Reactions(kinds = listOf(NostrEventKind.Reaction.value)),
    DMs(kinds = listOf(NostrEventKind.EncryptedDirectMessages.value)),
    Articles(kinds = listOf(NostrEventKind.LongFormContent.value)),
    All(kinds = null),
}
