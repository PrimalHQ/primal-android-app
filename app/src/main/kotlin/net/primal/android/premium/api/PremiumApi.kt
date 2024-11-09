package net.primal.android.premium.api

import net.primal.android.premium.api.model.CancelMembershipRequest
import net.primal.android.premium.api.model.MembershipStatusResponse
import net.primal.android.premium.api.model.NameAvailableResponse
import net.primal.android.premium.api.model.PurchaseMembershipRequest

interface PremiumApi {

    suspend fun isPrimalNameAvailable(name: String): NameAvailableResponse
    suspend fun changePrimalName(userId: String, name: String): NameAvailableResponse

    suspend fun getPremiumMembershipStatus(userId: String): MembershipStatusResponse?

    suspend fun purchaseMembership(userId: String, body: PurchaseMembershipRequest)

    suspend fun getMembershipProducts()

    suspend fun cancelMembership(userId: String, body: CancelMembershipRequest)
}
