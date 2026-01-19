package net.primal.data.account.repository.repository.factory

import net.primal.data.account.local.db.AccountDatabase

internal actual fun provideAccountDatabase(): AccountDatabase = error("AccountDatabase is not supported for desktop.")
