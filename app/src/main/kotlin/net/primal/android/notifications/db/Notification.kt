package net.primal.android.notifications.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.feed.db.MediaResource
import net.primal.android.feed.db.NostrResource
import net.primal.android.feed.db.PostData
import net.primal.android.feed.db.PostStats
import net.primal.android.profile.db.PostUserStats
import net.primal.android.profile.db.ProfileData

data class Notification(

    @Embedded
    val data: NotificationData,

    @Relation(entityColumn = "ownerId", parentColumn = "actionUserId")
    val actionByUser: ProfileData?,

    @Relation(entityColumn = "eventId", parentColumn = "actionUserId")
    val actionByUserResources: List<MediaResource> = emptyList(),

    @Relation(entityColumn = "postId", parentColumn = "actionPostId")
    val actionPost: PostData? = null,

    @Relation(entityColumn = "postId", parentColumn = "actionPostId")
    val actionPostStats: PostStats? = null,

    @Relation(entityColumn = "postId", parentColumn = "actionPostId")
    val actionPostUserStats: PostUserStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostMediaResources: List<MediaResource> = emptyList(),

    @Relation(entityColumn = "postId", parentColumn = "actionPostId")
    val actionPostNostrUris: List<NostrResource> = emptyList(),

    )
