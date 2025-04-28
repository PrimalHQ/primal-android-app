package net.primal.domain.notifications

import net.primal.domain.posts.FeedPost
import net.primal.domain.profile.ProfileData

data class Notification(
    val notificationId: String,
    val ownerId: String,
    val createdAt: Long,
    val type: NotificationType,
    val seenGloballyAt: Long? = null,
    val actionUserId: String? = null,
    val actionPostId: String? = null,
    val satsZapped: Long? = null,
    val actionByUser: ProfileData?,
    val actionOnPost: FeedPost?,
    val reaction: String? = null,
)
