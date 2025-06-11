package net.primal.wallet.data.repository

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.domain.WalletRepository

internal class WalletRepositoryImpl(
    private val database: WalletDatabase,
    private val walletPrimalApiClient: PrimalApiClient,
) : WalletRepository
