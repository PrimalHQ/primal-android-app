package net.primal.android.notifications.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.feed.db.PostData
import net.primal.android.note.db.EventStats
import net.primal.android.note.db.EventUserStats
import net.primal.android.profile.db.ProfileData

data class Notification(
    @Embedded
    val data: NotificationData,

    @Relation(entityColumn = "ownerId", parentColumn = "actionUserId")
    val actionByUser: ProfileData?,

    @Relation(entityColumn = "postId", parentColumn = "actionPostId")
    val actionPost: PostData? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionEventStats: EventStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostUserStats: EventUserStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostNoteAttachments: List<NoteAttachment> = emptyList(),

    @Relation(entityColumn = "postId", parentColumn = "actionPostId")
    val actionPostNostrUris: List<NoteNostrUri> = emptyList(),
)
