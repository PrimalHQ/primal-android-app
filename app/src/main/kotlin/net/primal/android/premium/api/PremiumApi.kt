package net.primal.android.premium.api

import net.primal.android.premium.api.model.CancelMembershipRequest
import net.primal.android.premium.api.model.LegendPaymentInstructionsResponse
import net.primal.android.premium.api.model.MembershipStatusResponse
import net.primal.android.premium.api.model.NameAvailableResponse
import net.primal.android.premium.api.model.PurchaseMembershipRequest
import net.primal.android.premium.domain.PremiumPurchaseOrder

interface PremiumApi {

    suspend fun isPrimalNameAvailable(name: String): NameAvailableResponse

    suspend fun changePrimalName(userId: String, name: String): NameAvailableResponse

    suspend fun getPremiumMembershipStatus(userId: String): MembershipStatusResponse?

    suspend fun purchaseMembership(userId: String, body: PurchaseMembershipRequest)

    suspend fun getPrimalLegendPaymentInstructions(
        userId: String,
        primalName: String,
    ): LegendPaymentInstructionsResponse

    suspend fun getMembershipProducts()

    suspend fun cancelMembership(userId: String, body: CancelMembershipRequest)

    suspend fun shouldShowSupportUs(): Boolean

    suspend fun getOrdersHistory(userId: String): List<PremiumPurchaseOrder>
}
