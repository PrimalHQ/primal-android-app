package net.primal.android.nostr.ext

import net.primal.android.feed.db.Nip19Entity
import net.primal.android.feed.db.PostData

fun List<PostData>.flatMapAsPostNip19Entities() = flatMap { postData ->
    postData.nip19Links.map { entity ->
        Nip19Entity(
            eventId = postData.postId,
            entity = entity,
            profile = null
        )
    }
}
