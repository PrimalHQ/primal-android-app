package net.primal.android.user.domain

import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.user.accounts.parseFollowings
import net.primal.android.user.accounts.parseInterests
import net.primal.android.user.accounts.parseRelays

fun NostrEvent.asUserAccount() = UserAccount(
    pubkey = pubKey,
    authorDisplayName = pubKey.asEllipsizedNpub(),
    userDisplayName = pubKey.asEllipsizedNpub(),
    relays = content.parseRelays(),
    following = tags?.parseFollowings() ?: emptySet(),
    interests = tags?.parseInterests() ?: emptyList(),
    contactsCreatedAt = createdAt,
)
