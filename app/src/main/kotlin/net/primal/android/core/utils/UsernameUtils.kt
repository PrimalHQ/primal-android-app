package net.primal.android.core.utils

import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.db.ProfileMetadata

fun ProfileMetadata.userNameUiFriendly(): String {
    return uiFriendlyUsername(this.displayName, this.handle, this.ownerId)
}

fun ContentMetadata.userNameUiFriendly(pubkey: String): String {
    return uiFriendlyUsername(this.displayName, this.name, pubkey)
}

private fun uiFriendlyUsername(displayName: String?, name: String?, pubkey: String): String {
    return when {
        displayName?.isNotEmpty() == true -> displayName
        name?.isNotEmpty() == true -> name
        else -> pubkey.hexToNpubHrp().asEllipsizedNpub()
    }
}
