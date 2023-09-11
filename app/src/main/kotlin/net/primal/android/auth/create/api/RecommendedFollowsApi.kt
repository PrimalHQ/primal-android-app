package net.primal.android.auth.create.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.auth.create.api.model.RecommendedFollowsResponse
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendedFollowsApi @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun fetch(username: String): RecommendedFollowsResponse {
        val getRequest = Request.Builder()
            .header("Content-Type", "application/json")
            .url("https://media.primal.net/api/suggestions?twitterhandle=$username")
            .get()
            .build()

        val response = withContext(Dispatchers.IO) {
            okHttpClient.newCall(getRequest).execute()
        }

        val responseBody = response.body
        return if (responseBody != null) {
            NostrJson.decodeFromStringOrNull(string = responseBody.string())
                ?: throw IOException("Invalid body content.")
        } else {
            throw IOException("Empty response body.")
        }
    }
}