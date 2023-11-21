package net.primal.android.user.accounts

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.user.api.UsersApi
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.domain.Relay
import org.junit.Test

class UserAccountFetcherTest {

    @Test
    fun `fetchUserProfile fetches user details for given pubkey`() = runTest {
        val expectedPubkey = "9b46c3f4a8dcdafdfff12a97c59758f38ff55002370fcfa7d14c8c857e9b5812"
        val expectedInternetIdentifier = "test@primal.net"
        val expectedFollowersCount = 123
        val expectedFollowingCount = 321
        val expectedNotesCount = 100
        val expectedPictureUrl = "https://test.com/image.jpg"
        val expectedName = "alex"
        val usersApiMock = mockk<UsersApi> {
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
                    sig = "invalidSig"
                ),
                profileStats = PrimalEvent(
                    kind = NostrEventKind.PrimalUserProfileStats.value,
                    content = "{" +
                            "\"pubkey\": \"$expectedPubkey\"," +
                            "\"follows_count\":$expectedFollowingCount," +
                            "\"followers_count\":$expectedFollowersCount," +
                            "\"note_count\":$expectedNotesCount," +
                            "\"time_joined\":null" +
                            "}",
                )
            )
        }

        val fetcher = UserAccountFetcher(usersApi = usersApiMock)
        val actual = fetcher.fetchUserProfile(pubkey = expectedPubkey)

        actual.authorDisplayName shouldBe expectedName
        actual.userDisplayName shouldBe expectedName
        val avatarCdnImage = actual.avatarCdnImage
        avatarCdnImage.shouldNotBeNull()
        avatarCdnImage.sourceUrl shouldBe expectedPictureUrl
        actual.internetIdentifier shouldBe expectedInternetIdentifier
        actual.followersCount shouldBe expectedFollowersCount
        actual.followingCount shouldBe expectedFollowingCount
        actual.notesCount shouldBe expectedNotesCount
    }

    @Test
    fun `fetchUserProfile fails if api call fails`() = runTest {
        val usersApiMock = mockk<UsersApi> {
            coEvery { getUserProfile(any()) } throws WssException()
        }
        val fetcher = UserAccountFetcher(usersApi = usersApiMock)
        shouldThrow<WssException> { fetcher.fetchUserProfile(pubkey = "any") }
    }

    @Test
    fun `fetchUserContacts fetches user contacts for given pubkey`() = runTest {
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
        val expectedRelays = listOf(
            Relay(
                url = "wss://relay.snort.social",
                read = true,
                write = false,
            ),
            Relay(
                url = "wss://relay.primal.net",
                read = false,
                write = true,
            ),
        )

        val usersApiMock = mockk<UsersApi> {
            coEvery { getUserContacts(any(), any()) } returns UserContactsResponse(
                contactsEvent = NostrEvent(
                    id = "invalidId",
                    pubKey = expectedPubkey,
                    createdAt = 1683463925,
                    kind = 3,
                    tags = expectedTags,
                    content = """
                    {
                        "${expectedRelays[0].url}": {
                            "read": ${expectedRelays[0].read},
                            "write": ${expectedRelays[0].write}
                        },
                        "${expectedRelays[1].url}": {
                            "read": ${expectedRelays[1].read},
                            "write": ${expectedRelays[1].write}
                        }
                     }
                     """.trimIndent(),
                    sig = "invalidSig"
                ),
            )
        }

        val fetcher = UserAccountFetcher(usersApi = usersApiMock)
        val actual = fetcher.fetchUserContacts(pubkey = expectedPubkey)

        actual.shouldNotBeNull()
        actual.relays shouldBe expectedRelays
        actual.following shouldBe expectedFollowing
        actual.interests shouldBe listOf("#bitcoin")
    }

    @Test
    fun `fetchUserContacts fails if api call fails`() = runTest {
        val usersApiMock = mockk<UsersApi> {
            coEvery { getUserContacts(any(), any()) } throws WssException()
        }
        val fetcher = UserAccountFetcher(usersApi = usersApiMock)
        shouldThrow<WssException> { fetcher.fetchUserContacts(pubkey = "any") }
    }

}
