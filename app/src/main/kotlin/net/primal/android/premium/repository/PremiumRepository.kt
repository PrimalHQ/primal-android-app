package net.primal.android.premium.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.premium.api.PremiumApi
import net.primal.android.premium.api.model.ContentNameAvailable

class PremiumRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val premiumApi: PremiumApi,
) {
    suspend fun isPrimalNameAvailable(name: String): Boolean =
        withContext(dispatchers.io()) {
            val response = premiumApi.isPrimalNameAvailable(name = name)

            val availableEvent = NostrJson.decodeFromStringOrNull<ContentNameAvailable>(
                response.membershipAvailableEvent?.content,
            )

            availableEvent?.available == true
        }
}
