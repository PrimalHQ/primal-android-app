package net.primal.android.user.updater

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.UserRepository
import java.time.Instant

class UserDataUpdater @AssistedInject constructor(
    @Assisted private val userId: String,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
) {

    private var lastTimeFetched: Instant = Instant.EPOCH

    private fun isUserDataSyncedInLast(seconds: Long): Boolean {
        return lastTimeFetched < Instant.now().minusSeconds(seconds)
    }

    suspend fun updateUserDataWithDebounce(timeoutInSeconds: Long) {
        if (isUserDataSyncedInLast(seconds = timeoutInSeconds)) {
            updateData()
            lastTimeFetched = Instant.now()
        }
    }

    private suspend fun updateData() {
        settingsRepository.fetchAndUpsertAppSettings(pubkey = userId)
        userRepository.fetchAndUpsertUserAccount(userId = userId)
    }
}
