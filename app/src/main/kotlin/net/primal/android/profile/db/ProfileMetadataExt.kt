package net.primal.android.profile.db

import net.primal.android.core.utils.asEllipsizedNpub


fun ProfileMetadata.authorNameUiFriendly(): String {
    return when {
        displayName?.isNotEmpty() == true -> displayName
        handle?.isNotEmpty() == true -> handle
        else -> ownerId.asEllipsizedNpub()
    }
}

fun ProfileMetadata.userNameUiFriendly(): String {
    return when {
        handle?.isNotEmpty() == true -> handle
        displayName?.isNotEmpty() == true -> displayName
        else -> ownerId.asEllipsizedNpub()
    }
}
