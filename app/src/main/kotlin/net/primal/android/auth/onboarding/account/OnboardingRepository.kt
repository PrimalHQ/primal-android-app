package net.primal.android.auth.onboarding.account

import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.auth.onboarding.account.api.OnboardingApi
import net.primal.android.auth.onboarding.account.api.asFollowPacks
import net.primal.android.auth.onboarding.account.ui.model.OnboardingFollowPack
import net.primal.core.caching.MediaCacher
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.remote.api.users.UsersApi

class OnboardingRepository @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val onboardingApi: OnboardingApi,
    private val mediaCacher: MediaCacher?,
    private val usersApi: UsersApi,
) {

    suspend fun fetchDefaultRelays(): List<String> =
        withContext(dispatchers.io()) {
            usersApi.getDefaultRelays()
        }

    suspend fun fetchFollowPacks(): List<OnboardingFollowPack> =
        withContext(dispatchers.io()) {
            val response = retryNetworkCall(retries = 3, retryOnException = IOException::class) {
                onboardingApi.getFollowSuggestions()
            }
            val followPacks = response.asFollowPacks()
            preCacheMedia(followPacks)

            followPacks
        }

    private fun preCacheMedia(followPacks: List<OnboardingFollowPack>) {
        val coverUrls = followPacks.mapNotNull { it.coverUrl }

        if (coverUrls.isNotEmpty()) mediaCacher?.preCacheFeedMedia(coverUrls)
    }
}
