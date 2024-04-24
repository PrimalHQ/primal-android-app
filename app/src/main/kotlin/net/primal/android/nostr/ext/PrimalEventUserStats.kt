package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats
import net.primal.android.note.db.NoteUserStats

fun ContentPrimalEventUserStats.asNoteUserStatsPO(userId: String) =
    NoteUserStats(
        postId = this.eventId,
        userId = userId,
        liked = this.liked,
        zapped = this.zapped,
        reposted = this.reposted,
        replied = this.replied,
    )

fun List<PrimalEvent>.mapNotNullAsNoteUserStatsPO(userId: String) =
    this.mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventUserStats>(it.content) }
        .map { it.asNoteUserStatsPO(userId = userId) }
