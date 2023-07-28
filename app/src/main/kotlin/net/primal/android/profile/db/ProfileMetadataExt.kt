package net.primal.android.profile.db

import net.primal.android.core.utils.asEllipsizedNpub


fun ProfileMetadata.authorNameUiFriendly(): String {
    return when {
        displayName?.isNotEmpty() == true -> displayName
        name?.isNotEmpty() == true -> name
        else -> ownerId.asEllipsizedNpub()
    }
}

fun ProfileMetadata.userNameUiFriendly(): String {
    return when {
        name?.isNotEmpty() == true -> name
        displayName?.isNotEmpty() == true -> displayName
        else -> ownerId.asEllipsizedNpub()
    }
}
