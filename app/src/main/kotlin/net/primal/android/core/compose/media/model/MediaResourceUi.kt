package net.primal.android.core.compose.media.model

import net.primal.android.feed.db.MediaResource
import net.primal.android.nostr.model.primal.PrimalResourceVariant

data class MediaResourceUi(
    val url: String,
    val mimeType: String? = null,
    val variants: List<PrimalResourceVariant> = emptyList(),
)

fun MediaResource.asMediaResourceUi() =
    MediaResourceUi(
        url = this.url,
        mimeType = this.contentType,
        variants = this.variants ?: emptyList(),
    )
