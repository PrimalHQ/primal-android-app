package net.primal.android.core.utils

import android.content.Context
import android.content.Intent
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.crypto.hexToNpubHrp

fun systemShareText(context: Context, text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

fun resolvePrimalNoteLink(noteId: String) = "https://primal.net/e/${noteId.hexToNoteHrp()}"

fun resolvePrimalProfileLink(profileId: String) = "https://primal.net/p/${profileId.hexToNpubHrp()}"
