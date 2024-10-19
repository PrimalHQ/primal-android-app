package net.primal.android.explore.domain

import java.time.Instant
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.notes.db.PostData
import net.primal.android.profile.db.ProfileData

data class ExploreZapNoteData(
    val sender: ProfileData?,
    val receiver: ProfileData?,
    val noteData: PostData,
    val amountSats: ULong,
    val zapMessage: String?,
    val createdAt: Instant,
    val noteNostrUris: List<NoteNostrUri> = emptyList(),
)
