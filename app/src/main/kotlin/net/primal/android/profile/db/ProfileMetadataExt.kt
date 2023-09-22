package net.primal.android.profile.db

import net.primal.android.core.utils.asEllipsizedNpub


fun ProfileMetadata.authorNameUiFriendly(): String {
    return when {
        displayName?.isNotEmpty() == true -> displayName
        handle?.isNotEmpty() == true -> handle
        else -> ownerId.asEllipsizedNpub()
    }
}
