package net.primal.data.repository.mappers

import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.notes.FeedAuthorLite
import net.primal.data.local.dao.notes.FeedAuthorNameLite
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.domain.nostr.utils.authorNameUiFriendly
import net.primal.domain.nostr.utils.usernameUiFriendly

fun ProfileData.usernameUiFriendly(): String = usernameUiFriendly(this.displayName, this.handle, this.ownerId)

fun ProfileData.authorNameUiFriendly(): String = authorNameUiFriendly(this.displayName, this.handle, this.ownerId)

fun FeedAuthorLite.usernameUiFriendly(): String = usernameUiFriendly(this.displayName, this.handle, this.ownerId)

fun FeedAuthorLite.authorNameUiFriendly(): String = authorNameUiFriendly(this.displayName, this.handle, this.ownerId)

fun FeedAuthorNameLite.usernameUiFriendly(): String = usernameUiFriendly(this.displayName, this.handle, this.ownerId)

fun FeedAuthorNameLite.authorNameUiFriendly(): String =
    authorNameUiFriendly(this.displayName, this.handle, this.ownerId)

fun EventZap.usernameUiFriendly(): String =
    usernameUiFriendly(this.zapSenderDisplayName, this.zapSenderHandle, this.zapSenderId)

fun EventZap.authorNameUiFriendly(): String =
    authorNameUiFriendly(this.zapSenderDisplayName, this.zapSenderHandle, this.zapSenderId)
