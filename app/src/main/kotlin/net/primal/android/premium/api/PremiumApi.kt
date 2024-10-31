package net.primal.android.premium.api

import net.primal.android.premium.api.model.NameAvailableResponse

interface PremiumApi {

    suspend fun isPrimalNameAvailable(name: String): NameAvailableResponse
}
