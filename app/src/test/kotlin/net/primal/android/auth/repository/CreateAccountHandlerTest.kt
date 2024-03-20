package net.primal.android.auth.repository

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.auth.onboarding.account.api.Suggestion
import net.primal.android.auth.onboarding.account.api.SuggestionMember
import net.primal.android.core.FakeDataStore
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.crypto.CryptoUtils
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.Credential
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateAccountHandlerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private fun createAccountHandler(
        authRepository: AuthRepository = mockk(relaxed = true),
        relayRepository: RelayRepository = mockk(relaxed = true),
        userRepository: UserRepository = mockk(relaxed = true),
        profileRepository: ProfileRepository = mockk(relaxed = true),
        settingsRepository: SettingsRepository = mockk(relaxed = true),
    ): CreateAccountHandler {
        return CreateAccountHandler(
            authRepository = authRepository,
            relayRepository = relayRepository,
            userRepository = userRepository,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
        )
    }

    private fun createAuthRepository(
        credentialsStore: CredentialsStore = CredentialsStore(FakeDataStore(emptyList())),
        activeAccountStore: ActiveAccountStore = mockk(relaxed = true),
        userRepository: UserRepository = mockk(relaxed = true),
    ) = AuthRepository(
        credentialsStore = credentialsStore,
        activeAccountStore = activeAccountStore,
        userRepository = userRepository,
    )

    @Test
    fun createNostrAccount_logsUserIn() = runTest {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val authRepository = mockk<AuthRepository>(relaxed = true)
        val handler = createAccountHandler(
            authRepository = authRepository,
        )
        handler.createNostrAccount(
            privateKey = keyPair.privateKey,
            profileMetadata = ProfileMetadata(displayName = "Test", username = null),
            interests = emptyList(),
        )

        coVerify {
            authRepository.login(
                withArg { it shouldBe keyPair.privateKey }
            )
        }
    }

    @Test
    fun createNostrAccount_setsGivenProfileMetadata() = runTest {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val handler = createAccountHandler(
            authRepository = createAuthRepository(),
            userRepository = userRepository,
        )

        val expectedProfileMetadata = ProfileMetadata(displayName = "Test", username = null)
        handler.createNostrAccount(
            privateKey = keyPair.privateKey,
            profileMetadata = expectedProfileMetadata,
            interests = emptyList(),
        )

        coVerify {
            userRepository.setProfileMetadata(
                withArg { it shouldBe keyPair.pubKey },
                withArg { it shouldBe expectedProfileMetadata },
            )
        }
    }

    @Test
    fun createNostrAccount_alwaysFollowsSelf() = runTest {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val profileRepository = mockk<ProfileRepository>(relaxed = true)
        val handler = createAccountHandler(
            authRepository = createAuthRepository(),
            profileRepository = profileRepository,
        )

        handler.createNostrAccount(
            privateKey = keyPair.privateKey,
            profileMetadata = ProfileMetadata(displayName = "Test", username = null),
            interests = emptyList(),
        )

        coVerify {
            profileRepository.setFollowList(
                withArg { it shouldBe keyPair.pubKey },
                withArg { it shouldContain keyPair.pubKey },
            )
        }
    }

    @Test
    fun createNostrAccount_followsEveryoneFromInterestsLists() = runTest {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val profileRepository = mockk<ProfileRepository>(relaxed = true)
        val handler = createAccountHandler(
            authRepository = createAuthRepository(),
            profileRepository = profileRepository,
        )

        val interestsList = listOf(Suggestion(group = "dev", members = primalTeamMembers))
        handler.createNostrAccount(
            privateKey = keyPair.privateKey,
            profileMetadata = ProfileMetadata(displayName = "Test", username = null),
            interests = interestsList,
        )

        coVerify {
            profileRepository.setFollowList(
                withArg { it shouldBe keyPair.pubKey },
                withArg {
                    it shouldContain keyPair.pubKey
                    it shouldContainAll primalTeamMembers.map { member -> member.userId }
                }
            )
        }
    }

    @Test
    fun createNostrAccount_bootstrapsTheDefaultRelays_withProperUserId() = runTest {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val relayRepository = mockk<RelayRepository>(relaxed = true)
        val handler = createAccountHandler(
            authRepository = createAuthRepository(),
            relayRepository = relayRepository,
        )

        handler.createNostrAccount(
            privateKey = keyPair.privateKey,
            profileMetadata = ProfileMetadata(displayName = "Test", username = null),
            interests = emptyList(),
        )

        coVerify {
            relayRepository.bootstrapDefaultUserRelays(
                withArg { it shouldBe keyPair.pubKey },
            )
        }
    }

    @Test
    fun createNostrAccount_fetchesAppSettings_withProperUserId() = runTest {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val settingsRepository = mockk<SettingsRepository>(relaxed = true)
        val handler = createAccountHandler(
            authRepository = createAuthRepository(),
            settingsRepository = settingsRepository,
        )

        handler.createNostrAccount(
            privateKey = keyPair.privateKey,
            profileMetadata = ProfileMetadata(displayName = "Test", username = null),
            interests = emptyList(),
        )

        coVerify {
            settingsRepository.fetchAndPersistAppSettings(
                withArg { it shouldBe keyPair.pubKey },
            )
        }
    }

    @Test
    fun createNostrAccount_revertsAuthData_ifAnyOfApiCallsFail() = runTest {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val credentialsPersistence = FakeDataStore(emptyList<Credential>())
        val credentialsStore = CredentialsStore(persistence = credentialsPersistence)

        val activeAccountPersistence = FakeDataStore(initialValue = "")
        val activeAccountStore = ActiveAccountStore(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            accountsStore = mockk(relaxed = true),
            persistence = activeAccountPersistence,
        )

        val authRepository = createAuthRepository(
            credentialsStore = credentialsStore,
            activeAccountStore = activeAccountStore,
        )
        val profileRepository = mockk<ProfileRepository>(relaxed = true) {
            coEvery { setFollowList(any(), any()) } throws WssException()
        }
        val handler = createAccountHandler(
            authRepository = authRepository,
            profileRepository = profileRepository,
        )

        try {
            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                interests = emptyList(),
            )
        } catch (_: CreateAccountHandler.AccountCreationException) { }

        credentialsPersistence.latestData shouldBe emptyList()
        activeAccountPersistence.latestData shouldBe ""
    }


    private val primalTeamMembers = listOf(
        SuggestionMember(
            name = "alex",
            userId = "npub1ky9s6hjl46wxcj9gcalhuk4ag2nea9yqufdyp9q9r496fns5g44sw0alex",
        ),
        SuggestionMember(
            name = "moysie",
            userId = "npub19f2765hdx8u9lz777w7azed2wsn9mqkf2gvn67mkldx8dnxvggcsmhe9da",
        ),
        SuggestionMember(
            name = "pavle",
            userId = "npub1k6tqlj78cpznd0yc74wy3k0elmj4nql87a3uzfz98tmj3tuzxywsf0dhk6",
        ),
        SuggestionMember(
            name = "pedja",
            userId = "npub1mkde3807tcyyp2f98re7n7z0va8979atqkfja7avknvwdjg97vpq6ef0jp",
        ),
        SuggestionMember(
            name = "marko",
            userId = "npub1zga04e73s7ard4kaektaha9vckdwll3y8auztyhl3uj764ua7vrqc7ppvc",
        ),
        SuggestionMember(
            name = "miljan",
            userId = "npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr",
        ),
    )
}
