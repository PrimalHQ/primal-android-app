package net.primal.android.auth.repository

import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.auth.onboarding.account.ui.model.FollowGroup
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.crypto.assureValidNsec
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import timber.log.Timber

class CreateAccountHandler @Inject constructor(
    private val authRepository: AuthRepository,
    private val relayRepository: RelayRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val credentialsStore: CredentialsStore,
    private val dispatchers: CoroutineDispatcherProvider,
    private val nostrNotary: NostrNotary,
) {

    suspend fun createNostrAccount(
        privateKey: String,
        profileMetadata: ProfileMetadata,
        interests: List<FollowGroup>,
    ) = withContext(dispatchers.io()) {
        runCatching {
            val userId = credentialsStore.saveNsec(nostrKey = privateKey)
            val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
                userId = userId,
                description = "Sync app settings",
            )

            relayRepository.bootstrapUserRelays(userId)
            userRepository.setProfileMetadata(userId = userId, profileMetadata = profileMetadata)
            val contacts = setOf(userId) + interests.mapToContacts()
            profileRepository.setFollowList(userId = userId, contacts = contacts)
            settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
        }.onFailure { exception ->
            Timber.w(exception)
            credentialsStore.removeCredentialByNsec(nsec = privateKey.assureValidNsec())
            throw AccountCreationException(cause = exception)
        }.onSuccess {
            authRepository.loginWithNsec(nostrKey = privateKey)
        }
    }

    private fun List<FollowGroup>.mapToContacts(): Set<String> {
        return flatMap {
            it.members
                .filter { member -> member.followed }
                .map { member -> member.userId }
        }
            .toSet()
    }

    class AccountCreationException(cause: Throwable) : IOException(cause)
}
