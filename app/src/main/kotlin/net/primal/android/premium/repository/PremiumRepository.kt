package net.primal.android.premium.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.parseAndFoldPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndFoldPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndFoldPrimalUserNames
import net.primal.android.nostr.ext.parseAndMapAsLeaderboardLegendEntries
import net.primal.android.nostr.ext.parseAndMapAsOGLeaderboardEntries
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.premium.api.PremiumApi
import net.primal.android.premium.api.model.CancelMembershipRequest
import net.primal.android.premium.api.model.LegendLeaderboardOrderBy
import net.primal.android.premium.api.model.MembershipStatusResponse
import net.primal.android.premium.api.model.PremiumLeaderboardOrderBy
import net.primal.android.premium.api.model.PurchaseMembershipRequest
import net.primal.android.premium.api.model.UpdatePrimalLegendProfileRequest
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.wallet.store.domain.SubscriptionPurchase
import net.primal.core.networking.utils.retryNetworkCall
import timber.log.Timber

class PremiumRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val premiumApi: PremiumApi,
    private val accountsStore: UserAccountsStore,
    private val database: PrimalDatabase,
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

    suspend fun fetchMembershipStatus(userId: String): PremiumMembership? =
        withContext(dispatchers.io()) {
            try {
                premiumApi.getPremiumMembershipStatus(userId = userId)?.let { response ->
                    accountsStore.getAndUpdateAccount(userId = userId) {
                        this.copy(premiumMembership = response.toPremiumMembership())
                    }
                }?.premiumMembership
            } catch (error: MissingPrivateKey) {
                Timber.w(error)
                null
            }
        }

    suspend fun shouldShowSupportUsNotice() =
        withContext(dispatchers.io()) {
            premiumApi.shouldShowSupportUs()
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
                    onChain = null,
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

    suspend fun fetchPremiumLeaderboard(
        orderBy: PremiumLeaderboardOrderBy = PremiumLeaderboardOrderBy.PremiumSince,
        since: Long? = null,
        until: Long? = null,
        limit: Int = 100,
    ) = withContext(dispatchers.io()) {
        val response = premiumApi.getPremiumLeaderboard(orderBy = orderBy, limit = limit, since = since, until = until)

        val primalUserNames = response.primalUsernames.parseAndFoldPrimalUserNames()
        val primalPremiumInfo = response.primalPremiumInfoEvents.parseAndFoldPrimalPremiumInfo()
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profiles = response.profileMetadatas.mapAsProfileDataPO(
            cdnResources = cdnResources,
            primalUserNames = primalUserNames,
            primalPremiumInfo = primalPremiumInfo,
            primalLegendProfiles = emptyMap(),
            blossomServers = emptyMap(),
        )

        database.profiles().insertOrUpdateAll(profiles)

        response.orderedPremiumLeaderboardEvent.parseAndMapAsOGLeaderboardEntries(
            profiles = profiles.asMapByKey { it.ownerId },
        )
    }

    suspend fun fetchLegendLeaderboard(orderBy: LegendLeaderboardOrderBy, limit: Int = 1000) =
        withContext(dispatchers.io()) {
            val response = premiumApi.getLegendLeaderboard(orderBy = orderBy, limit = limit)

            val primalUserNames = response.primalUsernames.parseAndFoldPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfoEvents.parseAndFoldPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndFoldPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val profiles = response.profileMetadatas.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = emptyMap(),
            )

            database.profiles().insertOrUpdateAll(profiles)

            response.orderedLegendLeaderboardEvent.parseAndMapAsLeaderboardLegendEntries(
                profiles = profiles.asMapByKey { it.ownerId },
            )
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

    suspend fun fetchPrimalLegendPaymentInstructions(
        userId: String,
        primalName: String,
        onChain: Boolean = true,
        amountUsd: String?,
    ) = withContext(dispatchers.io()) {
        retryNetworkCall(retries = 2) {
            premiumApi.getPrimalLegendPaymentInstructions(
                userId = userId,
                primalName = primalName,
                onChain = onChain,
                amountUsd = amountUsd,
            )
        }
    }

    suspend fun fetchOrderHistory(userId: String) =
        withContext(dispatchers.io()) {
            premiumApi.getOrdersHistory(userId = userId)
        }

    suspend fun updateLegendProfile(userId: String, updateProfileRequest: UpdatePrimalLegendProfileRequest) =
        withContext(dispatchers.io()) {
            premiumApi.updateLegendProfile(
                userId = userId,
                updateProfileRequest = updateProfileRequest,
            )
        }

    suspend fun fetchRecoveryContactsList(userId: String) =
        withContext(dispatchers.io()) {
            premiumApi.getRecoveryContactsList(userId = userId)
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
            origin = this.origin,
            editedShoutout = this.editedShoutout,
            donatedBtc = this.donatedBtc,
        )
    }
}
