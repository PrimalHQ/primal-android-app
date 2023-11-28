package net.primal.android.user.updater

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import kotlin.time.Duration
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.UserRepository
import timber.log.Timber

class UserDataUpdater @AssistedInject constructor(
    @Assisted val userId: String,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
) {

    private var lastTimeFetched: Instant = Instant.EPOCH

    private fun isUserDataSyncedInLast(duration: Duration): Boolean {
        return lastTimeFetched < Instant.now().minusMillis(duration.inWholeMilliseconds)
    }

    suspend fun updateUserDataWithDebounce(duration: Duration) {
        if (isUserDataSyncedInLast(duration)) {
            try {
                updateData()
                lastTimeFetched = Instant.now()
            } catch (error: WssException) {
                Timber.i(error)
            }
        }
    }

    private suspend fun updateData() {
        settingsRepository.fetchAndPersistAppSettings(userId = userId)
        userRepository.fetchAndUpdateUserAccount(userId = userId)
    }
}
