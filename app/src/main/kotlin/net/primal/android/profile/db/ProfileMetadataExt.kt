package net.primal.android.profile.db

import net.primal.android.core.utils.asEllipsizedNpub


fun ProfileMetadata.displayNameUiFriendly(): String {
    return when {
        displayName?.isNotEmpty() == true -> displayName
        name?.isNotEmpty() == true -> name
        else -> ownerId.asEllipsizedNpub()
    }
}
