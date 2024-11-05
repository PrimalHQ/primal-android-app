package net.primal.android.premium.api

import net.primal.android.premium.api.model.MembershipStatusResponse
import net.primal.android.premium.api.model.NameAvailableResponse
import net.primal.android.premium.api.model.PurchaseMembershipRequest

interface PremiumApi {

    suspend fun isPrimalNameAvailable(name: String): NameAvailableResponse

    suspend fun getMembershipStatus(userId: String): MembershipStatusResponse

    suspend fun purchaseMembership(userId: String, body: PurchaseMembershipRequest)
}
