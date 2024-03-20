package net.primal.android.auth.repository

import java.io.IOException
import javax.inject.Inject
import net.primal.android.auth.onboarding.account.api.Suggestion
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import timber.log.Timber

class CreateAccountHandler @Inject constructor(
    private val authRepository: AuthRepository,
    private val relayRepository: RelayRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun createNostrAccount(
        privateKey: String,
        profileMetadata: ProfileMetadata,
        interests: List<Suggestion>,
    ) {
        val userId = authRepository.login(nostrKey = privateKey)
        val postCreateAccountResult = runCatching {
            userRepository.setProfileMetadata(userId = userId, profileMetadata = profileMetadata)
            val contacts = setOf(userId) + interests.mapToContacts()
            profileRepository.setFollowList(userId = userId, contacts = contacts)
            relayRepository.bootstrapDefaultUserRelays(userId)
            settingsRepository.fetchAndPersistAppSettings(userId = userId)
        }

        val exception = postCreateAccountResult.exceptionOrNull()
        if (exception != null) {
            authRepository.logout()
            Timber.w(exception)
            throw AccountCreationException(cause = exception)
        }
    }

    private fun List<Suggestion>.mapToContacts(): Set<String> {
        return flatMap { it.members.map { member -> member.userId } }.toSet()
    }

    class AccountCreationException(cause: Throwable) : IOException(cause)
}
