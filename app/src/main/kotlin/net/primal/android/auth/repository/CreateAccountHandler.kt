package net.primal.android.auth.repository

import io.github.aakira.napier.Napier
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
import net.primal.core.utils.mapCatching
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.feeds.FeedSpecKind
import net.primal.domain.feeds.FeedsRepository
import net.primal.domain.feeds.PrimalFeed
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
    private val feedsRepository: FeedsRepository,
) {

    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())

    suspend fun createNostrAccount(
        privateKey: String,
        profileMetadata: ProfileMetadata,
        followedUserIds: Set<String>,
        preFetchedRelays: List<String>? = null,
        preFetchedNoteFeeds: List<PrimalFeed> = emptyList(),
    ) = withContext(dispatchers.io()) {
        runCatching {
            val userId = credentialsStore.saveNsec(nostrKey = privateKey)

            relayRepository.bootstrapUserRelays(userId, preFetchedRelays ?: FALLBACK_RELAY_URLS)

            coroutineScope {
                val lightningAddress = async { createWalletAndResolveLightningAddress(userId = userId) }
                awaitAll(
                    async { blossomRepository.ensureBlossomServerList(userId) },
                    async {
                        userRepository.setProfileMetadata(
                            userId = userId,
                            profileMetadata = lightningAddress.await()
                                ?.let { profileMetadata.copy(lightningAddress = it) }
                                ?: profileMetadata,
                        )
                    },
                    async { userRepository.setFollowList(userId = userId, contacts = setOf(userId) + followedUserIds) },
                    async {
                        persistDefaultNoteFeeds(userId = userId, preFetchedNoteFeeds = preFetchedNoteFeeds)
                    },
                )
            }

            scope.launchFetchSettings(userId)
        }.onFailure { exception ->
            Napier.w(throwable = exception) { "Failed to create Nostr account." }
            credentialsStore.removeCredentialByNsec(nsec = privateKey.assureValidNsec())
            throw AccountCreationException(cause = exception)
        }.onSuccess {
            authRepository.loginWithNsec(nostrKey = privateKey)
        }
    }

    /**
     * Seeds the default note feeds for the freshly created account, both locally and remotely.
     *
     * Persisting locally before the account becomes active keeps the home top app bar populated the
     * moment we land on the main screen. Publishing them makes sure the subsequent user feeds fetch
     * finds a real feed list instead of an empty one, which would wipe what we just persisted.
     *
     * Passing an empty [preFetchedNoteFeeds] is not a request to persist nothing: it makes
     * [FeedsRepository.fetchAndPersistDefaultFeeds] fetch the defaults inline instead. That is the
     * fallback for when onboarding could not prefetch them in time.
     *
     * Failures are swallowed on purpose - feeds are cosmetic and must never fail account creation.
     */
    private suspend fun persistDefaultNoteFeeds(userId: String, preFetchedNoteFeeds: List<PrimalFeed>) {
        runCatching {
            feedsRepository.fetchAndPersistDefaultFeeds(
                userId = userId,
                specKind = FeedSpecKind.Notes,
                givenDefaultFeeds = preFetchedNoteFeeds,
            )
        }.onFailure { error ->
            Napier.w(throwable = error) { "Failed to persist default note feeds during account creation." }
        }
    }

    private fun CoroutineScope.launchFetchSettings(userId: String) {
        launch {
            runCatching {
                withTimeout(BACKGROUND_TASK_TIMEOUT) { fetchSettings(userId) }
            }.onFailure { error ->
                Napier.w(throwable = error) { "Settings fetch timed out during onboarding." }
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

    private suspend fun createWalletAndResolveLightningAddress(userId: String): String? =
        ensureSparkWalletExistsUseCase.invoke(userId = userId)
            .mapCatching { walletId -> sparkWalletAccountRepository.getLightningAddress(userId, walletId) }
            .onFailure { error ->
                Napier.w(throwable = error) { "Failed to resolve lightning address for profile metadata." }
            }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }

    class AccountCreationException(cause: Throwable) : IOException(cause)

    private companion object {
        private val BACKGROUND_TASK_TIMEOUT = 30.seconds
    }
}
