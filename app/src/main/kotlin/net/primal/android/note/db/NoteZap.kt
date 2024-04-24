package net.primal.android.note.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.profile.db.ProfileData

data class NoteZap(

    @Embedded
    val data: NoteZapData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "zapSenderId",
    )
    val zapSender: ProfileData? = null,
)
