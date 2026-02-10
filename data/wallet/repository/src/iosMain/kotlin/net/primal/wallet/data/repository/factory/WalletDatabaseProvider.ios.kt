package net.primal.wallet.data.repository.factory

import net.primal.wallet.data.local.db.WalletDatabase

internal actual fun provideWalletDatabase(): WalletDatabase = IosRepositoryFactory.resolveWalletDatabase()
