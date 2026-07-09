package net.primal.data.repository

import net.primal.data.local.db.CachingDatabase
import net.primal.data.repository.feed.paging.FeedSpecInvalidationTracker
import net.primal.domain.user.UserDataCleanupRepository
import net.primal.shared.data.local.db.withTransaction

internal class UserDataCleanupRepositoryImpl(
    private val database: CachingDatabase,
    private val invalidationTracker: FeedSpecInvalidationTracker,
) : UserDataCleanupRepository {
    override suspend fun clearUserData(userId: String) {
        database.withTransaction {
            database.messages().deleteAllByOwnerId(ownerId = userId)
            database.messageConversations().deleteAllByOwnerId(ownerId = userId)
            database.feeds().deleteAllByOwnerId(ownerId = userId)
            database.mutedItems().deleteAllByOwnerId(ownerId = userId)
            database.notifications().deleteAllByOwnerId(ownerId = userId)
            database.notificationGroupCrossRef().deleteAllByOwnerId(ownerId = userId)
            database.articleFeedsConnections().deleteConnections(ownerId = userId)
            database.feedsConnections().deleteConnections(ownerId = userId)
            database.feedPostsRemoteKeys().deleteAllByOwnerId(ownerId = userId)
            database.publicBookmarks().deleteAllBookmarks(userId = userId)
        }
        invalidationTracker.invalidateAll()
    }
}
