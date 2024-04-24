package net.primal.android.user.accounts

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.user.api.UsersApi
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserAccountFetcherTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private lateinit var primalDatabase: PrimalDatabase

    private val expectedPubkey = "9b46c3f4a8dcdafdfff12a97c59758f38ff55002370fcfa7d14c8c857e9b5812"
    private val expectedInternetIdentifier = "test@primal.net"
    private val expectedFollowersCount = 123
    private val expectedFollowingCount = 321
    private val expectedNotesCount = 100
    private val expectedRepliesCount = 256
    private val expectedPictureUrl = "https://test.com/image.jpg"
    private val expectedName = "alex"

    private fun buildMockUserApi() = mockk<UsersApi> {
        coEvery { getUserProfile(any()) } returns UserProfileResponse(
            metadata = NostrEvent(
                id = "invalidId",
                pubKey = expectedPubkey,
                createdAt = 1683463925,
                kind = 0,
                tags = emptyList(),
                content = "{" +
                    "\"name\":\"$expectedName\"," +
                    "\"picture\":\"$expectedPictureUrl\"," +
                    "\"nip05\":\"$expectedInternetIdentifier\"" +
                    "}",
                sig = "invalidSig",
            ),
            profileStats = PrimalEvent(
                kind = NostrEventKind.PrimalUserProfileStats.value,
                content = "{" +
                    "\"pubkey\": \"$expectedPubkey\"," +
                    "\"follows_count\":$expectedFollowingCount," +
                    "\"followers_count\":$expectedFollowersCount," +
                    "\"note_count\":$expectedNotesCount," +
                    "\"reply_count\":$expectedRepliesCount," +
                    "\"time_joined\":1672527292," +
                    "\"relay_count\": 20," +
                    "\"total_zap_count\": 15793," +
                    "\"total_satszapped\": 7295853" +
                    "}",
            ),
        )
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        primalDatabase = Room.inMemoryDatabaseBuilder(context, PrimalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        primalDatabase.close()
    }

    @Test
    fun fetchUserProfile_fetchesUserDetailsForGivenPubkey() = runTest {
        val usersApiMock = buildMockUserApi()
        val fetcher = UserAccountFetcher(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            usersApi = usersApiMock,
            primalDatabase = primalDatabase,
        )
        val actual = fetcher.fetchUserProfileOrNull(userId = expectedPubkey)

        actual.shouldNotBeNull()
        actual.authorDisplayName shouldBe expectedName
        actual.userDisplayName shouldBe expectedName
        val avatarCdnImage = actual.avatarCdnImage
        avatarCdnImage.shouldNotBeNull()
        avatarCdnImage.sourceUrl shouldBe expectedPictureUrl
        actual.internetIdentifier shouldBe expectedInternetIdentifier
        actual.followersCount shouldBe expectedFollowersCount
        actual.followingCount shouldBe expectedFollowingCount
        actual.notesCount shouldBe expectedNotesCount
        actual.repliesCount shouldBe expectedRepliesCount
    }

    @Test
    fun fetchUserProfile_updatesUserProfileInDatabase() = runTest {
        val usersApiMock = buildMockUserApi()
        val fetcher = UserAccountFetcher(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            usersApi = usersApiMock,
            primalDatabase = primalDatabase,
        )
        fetcher.fetchUserProfileOrNull(userId = expectedPubkey)

        val profileData = primalDatabase.profiles().findProfileData(profileId = expectedPubkey)
        profileData.shouldNotBeNull()
        profileData.handle shouldBe expectedName
        val avatarCdnImage = profileData.avatarCdnImage
        avatarCdnImage.shouldNotBeNull()
        avatarCdnImage.sourceUrl shouldBe expectedPictureUrl
        profileData.internetIdentifier shouldBe expectedInternetIdentifier
    }

    @Test
    fun fetchUserProfile_updatesUserProfileStatsInDatabase() = runTest {
        val usersApiMock = buildMockUserApi()
        val fetcher = UserAccountFetcher(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            usersApi = usersApiMock,
            primalDatabase = primalDatabase,
        )
        fetcher.fetchUserProfileOrNull(userId = expectedPubkey)

        val profileData = primalDatabase.profileStats().observeProfileStats(profileId = expectedPubkey).first()
        profileData.shouldNotBeNull()

        profileData.followers shouldBe expectedFollowersCount
        profileData.following shouldBe expectedFollowingCount
        profileData.notesCount shouldBe expectedNotesCount
        profileData.repliesCount shouldBe expectedRepliesCount
    }

    @Test
    fun fetchUserProfile_failsIfApiCallFails() = runTest {
        val usersApiMock = mockk<UsersApi> {
            coEvery { getUserProfile(any()) } throws WssException()
        }
        val fetcher = UserAccountFetcher(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            usersApi = usersApiMock,
            primalDatabase = primalDatabase,
        )
        shouldThrow<WssException> { fetcher.fetchUserProfileOrNull(userId = "any") }
    }

    @Test
    fun fetchUserFollowList_fetchesUserFollowListForGivenPubkey() = runTest {
        val expectedPubkey = "9b46c3f4a8dcdafdfff12a97c59758f38ff55002370fcfa7d14c8c857e9b5812"
        val expectedFollowing = listOf(
            "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a",
            "dd9b989dfe5e0840a92538f3e9f84f674e5f17ab05932efbacb4d8e6c905f302",
            "97b988fbf4f8880493f925711e1bd806617b508fd3d28312288507e42f8a3368",
        )
        val expectedTags = listOf(
            buildJsonArray {
                add("p")
                add(expectedFollowing[0])
            },
            buildJsonArray {
                add("p")
                add(expectedFollowing[1])
            },
            buildJsonArray {
                add("p")
                add(expectedFollowing[2])
            },
            buildJsonArray {
                add("t")
                add("#bitcoin")
            },
        )
        val expectedContent = "something in the content. we don not care about it."

        val usersApiMock = mockk<UsersApi> {
            coEvery { getUserFollowList(any()) } returns UserContactsResponse(
                followListEvent = NostrEvent(
                    id = "invalidId",
                    pubKey = expectedPubkey,
                    createdAt = 1683463925,
                    kind = 3,
                    tags = expectedTags,
                    content = expectedContent,
                    sig = "invalidSig",
                ),
            )
        }

        val fetcher = UserAccountFetcher(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            usersApi = usersApiMock,
            primalDatabase = primalDatabase,
        )
        val actual = fetcher.fetchUserFollowListOrNull(userId = expectedPubkey)

        actual.shouldNotBeNull()
        actual.following shouldBe expectedFollowing
        actual.interests shouldBe listOf("#bitcoin")
    }

    @Test
    fun fetchUserFollowList_failsIfApiCallFails() = runTest {
        val usersApiMock = mockk<UsersApi> {
            coEvery { getUserFollowList(any()) } throws WssException()
        }
        val fetcher = UserAccountFetcher(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            usersApi = usersApiMock,
            primalDatabase = primalDatabase,
        )
        shouldThrow<WssException> { fetcher.fetchUserFollowListOrNull(userId = "any") }
    }

}
