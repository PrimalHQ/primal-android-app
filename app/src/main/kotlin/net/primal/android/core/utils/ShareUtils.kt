package net.primal.android.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

suspend fun systemShareImage(context: Context, bitmap: Bitmap) =
    runCatching {
        val file = withContext(Dispatchers.IO) {
            val shareDir = context.externalCacheDir
            val timestamp = System.currentTimeMillis()
            val imageFile = File(shareDir, "PrimalSharedImage_$timestamp.png")

            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_COMPRESSION_QUALITY, outputStream)
            }
            imageFile
        }

        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }

fun resolvePrimalNoteLink(noteId: String) = resolvePrimalNoteLink(nevent = Nevent(eventId = noteId))

fun resolvePrimalNoteLink(nevent: Nevent) = "https://primal.net/e/${nevent.toNeventString()}"

fun resolvePrimalArticleLink(naddr: String) = "https://primal.net/a/$naddr"

fun resolvePrimalProfileLink(profileId: String, primalName: String?): String {
    val path = primalName ?: "p/${Nprofile(pubkey = profileId).toNprofileString()}"
    return "https://primal.net/$path"
}
