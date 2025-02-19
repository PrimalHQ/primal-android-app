package net.primal.android.notes.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase

class PostRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
) {
    suspend fun findByPostId(postId: String) =
        withContext(dispatchers.io()) {
            database.posts().findByPostId(postId = postId)
        }
}
