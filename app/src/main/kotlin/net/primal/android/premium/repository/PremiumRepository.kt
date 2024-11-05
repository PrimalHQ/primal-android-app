package net.primal.android.premium.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.premium.api.PremiumApi
import net.primal.android.premium.api.model.MembershipStatusResponse
import net.primal.android.premium.api.model.PurchaseMembershipRequest
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.wallet.store.domain.SubscriptionPurchase

class PremiumRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val premiumApi: PremiumApi,
    private val accountsStore: UserAccountsStore,
) {
    suspend fun isPrimalNameAvailable(name: String): Boolean =
        withContext(dispatchers.io()) {
            val response = premiumApi.isPrimalNameAvailable(name = name)
            response.available
        }

    suspend fun fetchMembershipStatus(userId: String) {
        withContext(dispatchers.io()) {
            val response = premiumApi.getMembershipStatus(userId = userId)
            accountsStore.getAndUpdateAccount(userId = userId) {
                this.copy(premiumMembership = response.toPremiumMembership())
            }
        }
    }

    suspend fun purchaseMembership(
        userId: String,
        primalName: String,
        purchase: SubscriptionPurchase,
    ) {
        withContext(dispatchers.io()) {
            premiumApi.purchaseMembership(
                userId = userId,
                body = PurchaseMembershipRequest(
                    platform = "android",
                    name = primalName,
                    orderId = purchase.orderId,
                    purchaseToken = purchase.purchaseToken,
                    productId = purchase.productId,
                ),
            )
        }
    }

    private fun MembershipStatusResponse.toPremiumMembership(): PremiumMembership {
        return PremiumMembership(
            userId = this.pubkey,
            tier = this.tier,
            premiumName = this.name,
            nostrAddress = this.nostrAddress,
            lightningAddress = this.lightningAddress,
            vipProfile = this.primalVipProfile,
            usedStorageInBytes = this.usedStorage,
            maxStorageInBytes = this.maxStorage,
            expiresOn = this.expiresOn,
            cohort1 = this.cohort1,
            cohort2 = this.cohort2,
        )
    }
}
