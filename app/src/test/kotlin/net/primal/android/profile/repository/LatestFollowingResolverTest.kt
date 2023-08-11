package net.primal.android.profile.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.api.UsersApi
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.domain.UserAccount
import org.junit.Test
import java.time.Instant

class LatestFollowingResolverTest {

    private fun buildLatestFollowingResolver(
        usersApi: UsersApi = mockk(relaxed = true),
        activeAccountStore: ActiveAccountStore = mockk(relaxed = true) {
            every { activeUserAccount } returns MutableStateFlow(UserAccount.EMPTY)
        }
    ) = LatestFollowingResolver(
        usersApi = usersApi,
        activeAccountStore = activeAccountStore
    )

    private fun buildNostrContactsEvent(
        pubKey: String = "test-pubkey",
        content: String = "",
        createdAt: Long = 0,
        id: String = "test-id",
        kind: Int = NostrEventKind.Contacts.value,
        sig: String = "test-sig",
        tags: List<JsonArray>? = listOf()
    ) = NostrEvent(
        pubKey = pubKey,
        content = content,
        createdAt = createdAt,
        id = id,
        kind = kind,
        sig = sig,
        tags = tags
    )

    @Test
    fun `getLatestFollowing throws exception for missing contacts event`() = runTest {
        val resolver = buildLatestFollowingResolver(
            usersApi = mockk {
                coEvery { getUserContacts(any(), any()) } returns UserContactsResponse()
            },
        )

        shouldThrow<LatestFollowingResolver.RemoteFollowingsUnavailableException> {
            resolver.getLatestFollowing()
        }
    }

    @Test
    fun `getLatestFollowing returns remote contacts when newer available`() = runTest {
        val resolver = buildLatestFollowingResolver(
            usersApi = mockk {
                coEvery { getUserContacts(any(), any()) } returns UserContactsResponse(
                    contactsEvent = buildNostrContactsEvent(
                        createdAt = Instant.now().epochSecond,
                        tags = listOf(
                            buildJsonArray {
                                add("p")
                                add("pubkey1")
                            },
                            buildJsonArray {
                                add("p")
                                add("pubkey2")
                            },
                        ),
                    )
                )
            },
        )

        val actual = resolver.getLatestFollowing()
        actual.size shouldBe 2
        actual.shouldContain("pubkey1")
        actual.shouldContain("pubkey2")
    }

    @Test
    fun `getLatestFollowing returns local contacts when no newer available`() = runTest {
        val resolver = buildLatestFollowingResolver(
            activeAccountStore = mockk {
                coEvery { activeUserAccount() } returns UserAccount.EMPTY.copy(
                    contactsCreatedAt = Instant.now().epochSecond,
                    following = setOf("pubkey10", "pubkey11")
                )
            },
            usersApi = mockk {
                coEvery { getUserContacts(any(), any()) } returns UserContactsResponse(
                    contactsEvent = buildNostrContactsEvent(
                        createdAt = 0,
                        tags = listOf(
                            buildJsonArray {
                                add("p")
                                add("pubkey1")
                            },
                            buildJsonArray {
                                add("p")
                                add("pubkey2")
                            },
                        ),
                    )
                )
            },
        )

        val actual = resolver.getLatestFollowing()

        actual.size shouldBe 2
        actual.shouldContain("pubkey10")
        actual.shouldContain("pubkey11")
    }
}
