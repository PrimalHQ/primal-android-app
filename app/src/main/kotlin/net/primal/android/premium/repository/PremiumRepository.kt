package net.primal.android.premium.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.premium.api.PremiumApi
import net.primal.android.premium.api.model.CancelMembershipRequest
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

    suspend fun changePrimalName(userId: String, name: String): Boolean =
        withContext(dispatchers.io()) {
            val response = premiumApi.changePrimalName(userId = userId, name = name)

            response.available
        }

    suspend fun fetchMembershipStatus(userId: String) {
        withContext(dispatchers.io()) {
            premiumApi.getPremiumMembershipStatus(userId = userId)?.let { response ->
                accountsStore.getAndUpdateAccount(userId = userId) {
                    this.copy(premiumMembership = response.toPremiumMembership())
                }
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
                    name = primalName,
                    receiverUserId = userId,
                    primalProductId = null,
                    playSubscription = purchase.playSubscriptionJson,
                ),
            )
        }
    }

    suspend fun fetchMembershipProducts() {
        withContext(dispatchers.io()) {
            withContext(dispatchers.io()) {
                premiumApi.getMembershipProducts()
            }
        }
    }

    suspend fun cancelSubscription(userId: String, purchaseJson: String) {
        withContext(dispatchers.io()) {
            premiumApi.cancelMembership(
                userId = userId,
                body = CancelMembershipRequest(playSubscription = purchaseJson),
            )
            fetchMembershipStatus(userId = userId)
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
            renewsOn = this.renewsOn,
            cohort1 = this.cohort1,
            cohort2 = this.cohort2,
            recurring = this.recurring,
        )
    }
}
