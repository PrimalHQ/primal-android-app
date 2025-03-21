package net.primal.data.repository.mappers

import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.domain.common.utils.authorNameUiFriendly
import net.primal.domain.common.utils.usernameUiFriendly

fun ProfileData.usernameUiFriendly(): String = usernameUiFriendly(this.displayName, this.handle, this.ownerId)

fun ProfileData.authorNameUiFriendly(): String = authorNameUiFriendly(this.displayName, this.handle, this.ownerId)

fun EventZap.usernameUiFriendly(): String =
    usernameUiFriendly(this.zapSenderDisplayName, this.zapSenderHandle, this.zapSenderId)

fun EventZap.authorNameUiFriendly(): String =
    authorNameUiFriendly(this.zapSenderDisplayName, this.zapSenderHandle, this.zapSenderId)
