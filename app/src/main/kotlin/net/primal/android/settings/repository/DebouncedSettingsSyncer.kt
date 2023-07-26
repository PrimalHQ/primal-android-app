package net.primal.android.settings.repository

import java.time.Instant

class DebouncedSettingsSyncer(
    private val userId: String,
    private val repository: SettingsRepository
) {

    private var lastTimeFetched: Instant = Instant.EPOCH

    private fun isSettingsSyncedInLast(seconds: Long): Boolean {
        return lastTimeFetched < Instant.now().minusSeconds(seconds)
    }

    suspend fun updateSettingsWithDebounce(timeoutInSeconds: Long) {
        if (isSettingsSyncedInLast(seconds = timeoutInSeconds)) {
            repository.fetchAppSettings(pubkey = userId)
            lastTimeFetched = Instant.now()
        }
    }

}
