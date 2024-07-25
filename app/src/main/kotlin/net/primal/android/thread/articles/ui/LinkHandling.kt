package net.primal.android.thread.articles.ui

import android.net.Uri
import net.primal.android.nostr.ext.takeAsNaddrOrNull
import net.primal.android.nostr.ext.takeAsNoteHexIdOrNull
import net.primal.android.nostr.ext.takeAsProfileHexIdOrNull

fun String.handleArticleLinkClick(
    onProfileClick: ((profileId: String) -> Unit)?,
    onNoteClick: ((noteId: String) -> Unit)?,
    onArticleClick: ((naddr: String) -> Unit)?,
    onUrlClick: ((url: String) -> Unit)?,
) {
    Uri.parse(this)?.let { uri ->
        when (uri.scheme) {
            "nostr" -> {
                val url = this.split(":").lastOrNull().orEmpty()
                val profileId = url.takeAsProfileHexIdOrNull()
                val naddr = url.takeAsNaddrOrNull()
                val noteId = url.takeAsNoteHexIdOrNull()

                when {
                    profileId != null -> onProfileClick?.invoke(profileId)
                    noteId != null -> onNoteClick?.invoke(noteId)
                    naddr != null -> onArticleClick?.invoke(naddr)
                    else -> Unit
                }
            }

            else -> onUrlClick?.invoke(this)
        }
    }
}
