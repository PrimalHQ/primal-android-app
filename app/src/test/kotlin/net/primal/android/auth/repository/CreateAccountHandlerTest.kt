package net.primal.android.auth.repository

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.auth.onboarding.account.ui.model.FollowGroup
import net.primal.android.auth.onboarding.account.ui.model.FollowGroupMember
import net.primal.android.core.FakeDataStore
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.crypto.CryptoUtils
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.Credential
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
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
        credentialsStore: CredentialsStore = mockk(relaxed = true),
        nostrNotary: NostrNotary = mockk(relaxed = true),
    ): CreateAccountHandler {
        return CreateAccountHandler(
            authRepository = authRepository,
            relayRepository = relayRepository,
            userRepository = userRepository,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            credentialsStore = credentialsStore,
            dispatchers = coroutinesTestRule.dispatcherProvider,
            nostrNotary = nostrNotary,
        )
    }

    private fun createAuthRepository(
        credentialsStore: CredentialsStore = CredentialsStore(FakeDataStore(emptySet())),
        activeAccountStore: ActiveAccountStore = mockk(relaxed = true),
        userRepository: UserRepository = mockk(relaxed = true),
        accountsStore: UserAccountsStore = mockk(relaxed = true),
    ) = AuthRepository(
        credentialsStore = credentialsStore,
        activeAccountStore = activeAccountStore,
        userRepository = userRepository,
        accountsStore = accountsStore,
    )

    private fun createDummyNostrEvent(
        userId: String,
        kind: Int = NostrEventKind.ApplicationSpecificData.value,
    ): NostrEvent = NostrEvent(
        id = "",
        pubKey = userId,
        createdAt = 0,
        kind = kind,
        content = "",
        sig = "",
    )

    @Test
    fun createNostrAccount_logsUserIn() =
        runTest {
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
                authRepository.loginWithNsec(
                    withArg { it shouldBe keyPair.privateKey },
                )
            }
        }

    @Test
    fun createNostrAccount_setsGivenProfileMetadata() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val userRepository = mockk<UserRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val handler = createAccountHandler(
                userRepository = userRepository,
                credentialsStore = credentialsStore,
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
    fun createNostrAccount_alwaysFollowsSelf() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val profileRepository = mockk<ProfileRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val handler = createAccountHandler(
                authRepository = createAuthRepository(),
                profileRepository = profileRepository,
                credentialsStore = credentialsStore,
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
    fun createNostrAccount_followsEveryFollowedProfileFromInterestsLists() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val profileRepository = mockk<ProfileRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val handler = createAccountHandler(
                profileRepository = profileRepository,
                credentialsStore = credentialsStore,
            )

            val partiallyFollowedMembers = primalTeamMembers.mapIndexed { index, followGroupMember ->
                followGroupMember.copy(followed = index % 2 == 0)
            }
            val partiallyFollowedInterestsList = listOf(FollowGroup(name = "dev", members = partiallyFollowedMembers))

            val expectedMemberIds = partiallyFollowedMembers
                .filter { it.followed }
                .map { member -> member.userId } + keyPair.pubKey

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                interests = partiallyFollowedInterestsList,
            )

            coVerify {
                profileRepository.setFollowList(
                    withArg { it shouldBe keyPair.pubKey },
                    withArg {
                        println(it)
                        println(expectedMemberIds)
                        it shouldContainOnly expectedMemberIds
                    },
                )
            }
        }

    @Test
    fun createNostrAccount_bootstrapsTheDefaultRelays_withProperUserId() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val relayRepository = mockk<RelayRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val handler = createAccountHandler(
                relayRepository = relayRepository,
                credentialsStore = credentialsStore,
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                interests = emptyList(),
            )

            coVerify {
                relayRepository.bootstrapUserRelays(
                    withArg { it shouldBe keyPair.pubKey },
                )
            }
        }

    @Test
    fun createNostrAccount_fetchesAppSettings_withProperUserId() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val settingsRepository = mockk<SettingsRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val expectedNostrEvent = createDummyNostrEvent(userId = keyPair.pubKey)

            val handler = createAccountHandler(
                settingsRepository = settingsRepository,
                credentialsStore = credentialsStore,
                nostrNotary = mockk(relaxed = true) {
                    every {
                        signAuthorizationNostrEvent(keyPair.pubKey, any(), any())
                    } returns expectedNostrEvent
                },
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                interests = emptyList(),
            )

            coVerify {
                settingsRepository.fetchAndPersistAppSettings(
                    withArg { it.pubKey shouldBe keyPair.pubKey },
                )
            }
        }

    @Test
    fun createNostrAccount_revertsAuthData_ifAnyOfApiCallsFail() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val credentialsPersistence = FakeDataStore(emptySet<Credential>())
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
            } catch (_: CreateAccountHandler.AccountCreationException) {
            }

            credentialsPersistence.latestData shouldBe emptyList()
            activeAccountPersistence.latestData shouldBe ""
        }

    private val primalTeamMembers = listOf(
        FollowGroupMember(
            name = "alex",
            userId = "npub1ky9s6hjl46wxcj9gcalhuk4ag2nea9yqufdyp9q9r496fns5g44sw0alex",
        ),
        FollowGroupMember(
            name = "moysie",
            userId = "npub19f2765hdx8u9lz777w7azed2wsn9mqkf2gvn67mkldx8dnxvggcsmhe9da",
        ),
        FollowGroupMember(
            name = "pavle",
            userId = "npub1k6tqlj78cpznd0yc74wy3k0elmj4nql87a3uzfz98tmj3tuzxywsf0dhk6",
        ),
        FollowGroupMember(
            name = "pedja",
            userId = "npub1mkde3807tcyyp2f98re7n7z0va8979atqkfja7avknvwdjg97vpq6ef0jp",
        ),
        FollowGroupMember(
            name = "marko",
            userId = "npub1zga04e73s7ard4kaektaha9vckdwll3y8auztyhl3uj764ua7vrqc7ppvc",
        ),
        FollowGroupMember(
            name = "miljan",
            userId = "npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr",
        ),
    )
}
