package net.primal.android.wallet.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import java.time.Instant
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapNotNullAsWalletTransactionPO
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.api.UsersApi
import net.primal.android.user.api.model.UserProfilesResponse
import net.primal.android.wallet.api.WalletApi
import net.primal.android.wallet.api.model.TransactionsRequestBody
import net.primal.android.wallet.db.WalletTransaction
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.utils.CurrencyConversionUtils.formatAsString
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import timber.log.Timber

@ExperimentalPagingApi
class WalletTransactionsMediator(
    private val dispatchers: CoroutineDispatcherProvider,
    private val userId: String,
    private val accountsStore: UserAccountsStore,
    private val database: PrimalDatabase,
    private val walletApi: WalletApi,
    private val usersApi: UsersApi,
) : RemoteMediator<Int, WalletTransaction>() {

    private val lastRequests: MutableMap<LoadType, TransactionsRequestBody> = mutableMapOf()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, WalletTransaction>): MediatorResult {
        val walletLightningAddress = accountsStore.findByIdOrNull(userId)?.primalWallet?.lightningAddress
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        val timestamp: Long? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                state.firstItemOrNull()?.data?.createdAt
                    ?: withContext(dispatchers.io()) {
                        database.walletTransactions().first()?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                state.lastItemOrNull()?.data?.createdAt
                    ?: withContext(dispatchers.io()) {
                        database.walletTransactions().last()?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        if (timestamp == null && loadType != LoadType.REFRESH) {
            return MediatorResult.Error(IllegalStateException("Remote key not found."))
        }

        val initialRequestBody = TransactionsRequestBody(
            subWallet = SubWallet.Open,
            limit = state.config.pageSize,
            minAmountInBtc = accountsStore.findByIdOrNull(userId)
                ?.primalWalletSettings
                ?.spamThresholdAmountInSats?.toBtc()?.formatAsString(),
        )

        val requestBody = when (loadType) {
            LoadType.REFRESH -> initialRequestBody
            LoadType.PREPEND -> initialRequestBody.copy(since = timestamp, until = Instant.now().epochSecond)
            LoadType.APPEND -> initialRequestBody.copy(until = timestamp)
        }

        if (loadType != LoadType.REFRESH && lastRequests[loadType] == requestBody) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        val response = try {
            withContext(dispatchers.io()) {
                walletApi.getTransactions(userId = userId, body = requestBody)
            }
        } catch (error: WssException) {
            return MediatorResult.Error(error)
        }

        lastRequests[loadType] = requestBody

        withContext(dispatchers.io()) {
            val transactions = response.transactions.mapNotNullAsWalletTransactionPO(
                walletAddress = walletLightningAddress,
            )

            val mentionedUserIds = transactions.mapNotNull { it.otherUserId }.toSet()
            val profilesResponse = if (mentionedUserIds.isNotEmpty()) {
                try {
                    usersApi.getUserProfilesMetadata(userIds = mentionedUserIds)
                } catch (error: WssException) {
                    Timber.w(error)
                    UserProfilesResponse()
                }
            } else {
                UserProfilesResponse()
            }
            val cdnResources = profilesResponse.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val profiles = profilesResponse.metadataEvents.map { it.asProfileDataPO(cdnResources = cdnResources) }

            database.withTransaction {
                database.profiles().upsertAll(data = profiles)
                database.walletTransactions().upsertAll(data = transactions)
            }
        }

        return MediatorResult.Success(endOfPaginationReached = false)
    }
}
