package net.primal.data.utils

import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.domain.common.utils.authorNameUiFriendly
import net.primal.domain.common.utils.usernameUiFriendly
import net.primal.domain.nostr.ContentMetadata

// TODO Clean & decouple ext functions into appropriate modules

fun ProfileData.usernameUiFriendly(): String = usernameUiFriendly(this.displayName, this.handle, this.ownerId)

fun ProfileData.authorNameUiFriendly(): String = authorNameUiFriendly(this.displayName, this.handle, this.ownerId)

// fun UserAccount.authorNameUiFriendly(): String =
//    authorNameUiFriendly(this.authorDisplayName, this.userDisplayName, this.pubkey)

fun EventZap.usernameUiFriendly(): String =
    usernameUiFriendly(this.zapSenderDisplayName, this.zapSenderHandle, this.zapSenderId)

fun EventZap.authorNameUiFriendly(): String =
    authorNameUiFriendly(this.zapSenderDisplayName, this.zapSenderHandle, this.zapSenderId)

fun ContentMetadata.usernameUiFriendly(pubkey: String) =
    usernameUiFriendly(
        displayName,
        name,
        pubkey,
    )

fun ContentMetadata.authorNameUiFriendly(pubkey: String): String =
    authorNameUiFriendly(
        displayName,
        name,
        pubkey,
    )

// fun ProfileMetadata.usernameUiFriendly(userId: String): String =
//    usernameUiFriendly(
//        displayName,
//        username,
//        userId,
//    )
//
// fun ProfileMetadata.authorNameUiFriendly(userId: String): String =
//    authorNameUiFriendly(
//        displayName,
//        username,
//        userId,
//    )
//
