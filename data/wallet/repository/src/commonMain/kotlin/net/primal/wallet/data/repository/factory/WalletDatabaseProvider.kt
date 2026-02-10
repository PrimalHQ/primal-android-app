package net.primal.wallet.data.repository.factory

import net.primal.wallet.data.local.db.WalletDatabase

internal expect fun provideWalletDatabase(): WalletDatabase
