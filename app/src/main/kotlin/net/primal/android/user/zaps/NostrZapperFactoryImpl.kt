package net.primal.android.user.zaps

import javax.inject.Inject
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.domain.WalletKycLevel
import net.primal.android.wallet.nwc.NwcNostrZapper
import net.primal.android.wallet.nwc.NwcNostrZapperFactory
import net.primal.android.wallet.repository.WalletNostrZapper
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.NostrZapperFactory

class NostrZapperFactoryImpl @Inject constructor(
    private val accountsStore: UserAccountsStore,
    private val nwcNostrZapperFactory: NwcNostrZapperFactory,
    private val primalWalletZapper: WalletNostrZapper,
) : NostrZapperFactory {

    override suspend fun createOrNull(userId: String): NostrZapper? {
        val userAccount = accountsStore.findByIdOrNull(userId = userId)
        val walletPreference = userAccount?.walletPreference
        return when (walletPreference) {
            WalletPreference.NostrWalletConnect -> userAccount.nostrWallet?.createNwcZapper()
            else -> userAccount?.primalWallet?.createPrimalWalletZapper()
        }
    }

    private fun PrimalWallet.createPrimalWalletZapper(): WalletNostrZapper? {
        return if (this.kycLevel != WalletKycLevel.None) {
            primalWalletZapper
        } else {
            null
        }
    }

    private fun NostrWalletConnect.createNwcZapper(): NwcNostrZapper? {
        return nwcNostrZapperFactory.create(nwcData = this)
    }
}
