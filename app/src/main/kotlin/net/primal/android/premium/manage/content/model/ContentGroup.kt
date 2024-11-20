package net.primal.android.premium.manage.content.model

import net.primal.android.nostr.model.NostrEventKind

enum class ContentGroup(val kinds: List<Int>?) {
    Notes(kinds = listOf(NostrEventKind.ShortTextNote.value)),
    Reactions(kinds = listOf(NostrEventKind.Reaction.value)),
    DMs(kinds = listOf(NostrEventKind.EncryptedDirectMessages.value)),
    Articles(kinds = listOf(NostrEventKind.LongFormContent.value)),
    All(kinds = null),
}
