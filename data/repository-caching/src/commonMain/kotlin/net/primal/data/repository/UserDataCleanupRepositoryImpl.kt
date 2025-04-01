package net.primal.data.repository

import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.domain.repository.UserDataCleanupRepository

class UserDataCleanupRepositoryImpl(
    private val database: PrimalDatabase,
) : UserDataCleanupRepository {
    override suspend fun clearUserData(userId: String) {
        database.withTransaction {
            database.messages().deleteAllByOwnerId(ownerId = userId)
            database.messageConversations().deleteAllByOwnerId(ownerId = userId)
            database.feeds().deleteAllByOwnerId(ownerId = userId)
            database.mutedUsers().deleteAllByOwnerId(ownerId = userId)
            database.notifications().deleteAllByOwnerId(ownerId = userId)
            database.articleFeedsConnections().deleteConnections(ownerId = userId)
            database.feedsConnections().deleteConnections(ownerId = userId)
            database.feedPostsRemoteKeys().deleteAllByOwnerId(ownerId = userId)
            database.publicBookmarks().deleteAllBookmarks(userId = userId)
        }
    }
}
