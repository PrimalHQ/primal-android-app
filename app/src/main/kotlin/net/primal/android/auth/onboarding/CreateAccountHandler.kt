package net.primal.android.auth.onboarding

import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.auth.AuthRepository
import net.primal.android.auth.onboarding.api.Suggestion
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import timber.log.Timber

class CreateAccountHandler @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val authRepository: AuthRepository,
    private val relayRepository: RelayRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun createNostrAccount(profileMetadata: ProfileMetadata, interests: List<Suggestion>): String {
        val userId = authRepository.createAccountAndLogin()
        try {
            withContext(dispatcherProvider.io()) {
                userRepository.setProfileMetadata(
                    userId = userId,
                    profileMetadata = profileMetadata,
                )
                profileRepository.setFollowList(
                    userId = userId,
                    contacts = setOf(userId) + interests.mapToContacts(),
                )
                relayRepository.bootstrapDefaultUserRelays(userId)
                settingsRepository.fetchAndPersistAppSettings(userId = userId)
            }
        } catch (error: WssException) {
            Timber.w(error)
            throw AccountCreationException(cause = error)
        } catch (error: NostrPublishException) {
            Timber.w(error)
            throw AccountCreationException(cause = error)
        }
        return userId
    }

    private fun List<Suggestion>.mapToContacts(): Set<String> {
        return flatMap { it.members.map { member -> member.pubkey } }.toSet()
    }

    class AccountCreationException(cause: Throwable) : IOException(cause)
}
