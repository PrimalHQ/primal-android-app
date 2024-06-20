package net.primal.android.read

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.read.api.ReadsApi
import net.primal.android.read.api.model.BlogThreadRequestBody

class ReadRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val readsApi: ReadsApi,
) {

    suspend fun fetchBlogContentAndReplies(
        userId: String,
        authorUserId: String,
        identifier: String,
    ) = withContext(dispatchers.io()) {
        readsApi.getBlogThread(
            body = BlogThreadRequestBody(
                userId = userId,
                authorUserId = authorUserId,
                identifier = identifier,
                kind = NostrEventKind.LongFormContent.value,
                limit = 100,
            ),
        )
    }
}
