package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.asSha256Hash
import net.primal.core.utils.runCatching
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.parser.parseNWCUrl
import net.primal.domain.wallet.NostrWalletKeypair
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository

class ConnectNwcUseCase(
    private val walletRepository: WalletRepository,
    private val walletAccountRepository: WalletAccountRepository,
) {
    suspend fun invoke(
        userId: String,
        nwcUrl: String,
        autoSetAsDefaultWallet: Boolean = true,
    ): Result<String> =
        runCatching {
            val nostrWalletConnect = nwcUrl.parseNWCUrl()
            val walletId = nostrWalletConnect.keypair.privateKey.asSha256Hash()

            walletRepository.upsertNostrWallet(
                userId = userId,
                wallet = Wallet.NWC(
                    walletId = walletId,
                    userId = userId,
                    lightningAddress = nostrWalletConnect.lightningAddress,
                    balanceInBtc = null,
                    maxBalanceInBtc = null,
                    spamThresholdAmountInSats = 1L,
                    lastUpdatedAt = null,
                    relays = nostrWalletConnect.relays,
                    pubkey = nostrWalletConnect.pubkey,
                    keypair = NostrWalletKeypair(
                        privateKey = nostrWalletConnect.keypair.privateKey,
                        pubkey = nostrWalletConnect.keypair.pubkey,
                    ),
                ),
            )

            if (autoSetAsDefaultWallet) {
                walletAccountRepository.setActiveWallet(
                    userId = userId,
                    walletId = walletId,
                )
            }

            walletId
        }
}
