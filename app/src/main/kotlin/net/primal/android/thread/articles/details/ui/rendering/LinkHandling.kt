package net.primal.android.thread.articles.details.ui.rendering

import android.net.Uri
import net.primal.domain.nostr.utils.takeAsNaddrStringOrNull
import net.primal.domain.nostr.utils.takeAsNoteHexIdOrNull
import net.primal.domain.nostr.utils.takeAsProfileHexIdOrNull

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
                val naddr = url.takeAsNaddrStringOrNull()
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
