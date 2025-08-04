package net.primal.android.auth.repository

import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.auth.onboarding.account.ui.model.FollowGroup
import net.primal.android.networking.UserAgentProvider
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.BlossomRepository
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.api.settings.model.AppSettingsDescription
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.assureValidNsec
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import timber.log.Timber

class CreateAccountHandler @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val credentialsStore: CredentialsStore,
    private val eventsSignatureHandler: NostrEventSignatureHandler,
    private val authRepository: AuthRepository,
    private val relayRepository: RelayRepository,
    private val blossomRepository: BlossomRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val walletAccountRepository: WalletAccountRepository,
) {

    suspend fun createNostrAccount(
        privateKey: String,
        profileMetadata: ProfileMetadata,
        interests: List<FollowGroup>,
    ) = withContext(dispatchers.io()) {
        runCatching {
            val userId = credentialsStore.saveNsec(nostrKey = privateKey)
            relayRepository.bootstrapUserRelays(userId)
            blossomRepository.ensureBlossomServerList(userId)
            userRepository.setProfileMetadata(userId = userId, profileMetadata = profileMetadata)
            val contacts = setOf(userId) + interests.mapToContacts()
            walletAccountRepository.fetchWalletAccountInfo(userId = userId)
            walletAccountRepository.setActiveWallet(userId = userId, walletId = userId)
            userRepository.setFollowList(userId = userId, contacts = contacts)
            settingsRepository.fetchAndPersistAppSettings(
                authorizationEvent = eventsSignatureHandler.signNostrEvent(
                    unsignedNostrEvent = NostrUnsignedEvent(
                        pubKey = userId,
                        kind = NostrEventKind.ApplicationSpecificData.value,
                        tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                        content = AppSettingsDescription(description = "Sync app settings").encodeToJsonString(),
                    ),
                ).unwrapOrThrow(),
            )
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
