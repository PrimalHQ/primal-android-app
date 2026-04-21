package net.primal.android.auth.repository

import io.github.aakira.napier.Napier
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.relays.FALLBACK_RELAY_URLS
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.BlossomRepository
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.assureValidNsec
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.settings.AppSettingsDescription
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase

@Suppress("LongParameterList")
class CreateAccountHandler @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val credentialsStore: CredentialsStore,
    private val eventsSignatureHandler: NostrEventSignatureHandler,
    private val authRepository: AuthRepository,
    private val relayRepository: RelayRepository,
    private val blossomRepository: BlossomRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
) {

    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())

    suspend fun createNostrAccount(
        privateKey: String,
        profileMetadata: ProfileMetadata,
        followedUserIds: Set<String>,
        preFetchedRelays: List<String>? = null,
    ) = withContext(dispatchers.io()) {
        runCatching {
            val userId = credentialsStore.saveNsec(nostrKey = privateKey)

            relayRepository.bootstrapUserRelays(userId, preFetchedRelays ?: FALLBACK_RELAY_URLS)

            val walletIdDeferred = scope.asyncEnsureWalletExists(userId)
            scope.launchFetchSettings(userId)

            coroutineScope {
                awaitAll(
                    async { blossomRepository.ensureBlossomServerList(userId) },
                    async { userRepository.setProfileMetadata(userId = userId, profileMetadata = profileMetadata) },
                    async { userRepository.setFollowList(userId = userId, contacts = setOf(userId) + followedUserIds) },
                )
            }

            scope.launchSetLightningAddressIfWalletReady(userId, walletIdDeferred)
        }.onFailure { exception ->
            Napier.w(throwable = exception) { "Failed to create Nostr account." }
            credentialsStore.removeCredentialByNsec(nsec = privateKey.assureValidNsec())
            throw AccountCreationException(cause = exception)
        }.onSuccess {
            authRepository.loginWithNsec(nostrKey = privateKey)
        }
    }

    private fun CoroutineScope.asyncEnsureWalletExists(userId: String): Deferred<String?> =
        async {
            try {
                withTimeout(BACKGROUND_TASK_TIMEOUT) {
                    ensureSparkWalletExistsUseCase.invoke(userId = userId)
                        .onFailure { error ->
                            Napier.w(throwable = error) { "Wallet creation failed during onboarding." }
                        }
                        .getOrNull()
                }
            } catch (error: TimeoutCancellationException) {
                Napier.w(throwable = error) { "Wallet creation timed out during onboarding." }
                null
            }
        }

    private fun CoroutineScope.launchFetchSettings(userId: String) {
        launch {
            try {
                withTimeout(BACKGROUND_TASK_TIMEOUT) { fetchSettings(userId) }
            } catch (error: TimeoutCancellationException) {
                Napier.w(throwable = error) { "Settings fetch timed out during onboarding." }
            }
        }
    }

    private fun CoroutineScope.launchSetLightningAddressIfWalletReady(
        userId: String,
        walletIdDeferred: Deferred<String?>,
    ) {
        launch {
            try {
                withTimeout(BACKGROUND_TASK_TIMEOUT) {
                    val walletId = walletIdDeferred.await() ?: return@withTimeout
                    setLightningAddress(userId = userId, walletId = walletId)
                }
            } catch (error: TimeoutCancellationException) {
                Napier.w(throwable = error) { "Set lightning address timed out during onboarding." }
            }
        }
    }

    private suspend fun fetchSettings(userId: String) {
        runCatching {
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
        }.onFailure { error ->
            Napier.w(throwable = error) { "Settings fetch failed during onboarding." }
        }
    }

    private suspend fun setLightningAddress(userId: String, walletId: String) {
        runCatching {
            val lightningAddress = sparkWalletAccountRepository.getLightningAddress(userId, walletId)
            if (!lightningAddress.isNullOrBlank()) {
                userRepository.setLightningAddress(userId = userId, lightningAddress = lightningAddress)
            }
        }.onFailure { error ->
            Napier.w(throwable = error) { "Failed to set lightning address in profile metadata." }
        }
    }

    class AccountCreationException(cause: Throwable) : IOException(cause)

    private companion object {
        private val BACKGROUND_TASK_TIMEOUT = 30.seconds
    }
}
