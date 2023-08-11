package net.primal.android.user.updater

import dagger.assisted.AssistedFactory

@AssistedFactory
interface UserDataUpdaterFactory {

    fun create(userId: String): UserDataUpdater
}
