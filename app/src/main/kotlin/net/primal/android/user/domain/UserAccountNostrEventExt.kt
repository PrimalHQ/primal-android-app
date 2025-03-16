package net.primal.android.user.domain

import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.user.accounts.parseFollowings
import net.primal.android.user.accounts.parseInterests
import net.primal.domain.nostr.NostrEvent

fun NostrEvent.asUserAccountFromFollowListEvent() =
    UserAccount(
        pubkey = pubKey,
        authorDisplayName = pubKey.asEllipsizedNpub(),
        userDisplayName = pubKey.asEllipsizedNpub(),
        following = tags.parseFollowings(),
        interests = tags.parseInterests(),
        followListEventContent = content,
    )
