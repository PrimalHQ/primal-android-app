package net.primal.android.core.utils

import android.content.Context
import android.content.Intent
import net.primal.android.nostr.utils.Nevent
import net.primal.android.nostr.utils.Nip19TLV.toNeventString
import net.primal.android.nostr.utils.Nip19TLV.toNprofileString
import net.primal.android.nostr.utils.Nprofile

fun systemShareText(context: Context, text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

fun resolvePrimalNoteLink(noteId: String) = resolvePrimalNoteLink(nevent = Nevent(eventId = noteId))

fun resolvePrimalNoteLink(nevent: Nevent) = "https://primal.net/e/${nevent.toNeventString()}"

fun resolvePrimalArticleLink(naddr: String) = "https://primal.net/a/$naddr"

fun resolvePrimalProfileLink(profileId: String, primalName: String?): String {
    val path = primalName ?: "p/${Nprofile(pubkey = profileId).toNprofileString()}"
    return "https://primal.net/$path"
}
