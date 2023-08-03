package net.primal.android.user.services

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import net.primal.android.networking.primal.PrimalApiException
import net.primal.android.networking.primal.PrimalQueryResult
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.user.accounts.parseFollowings
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.impl.ContactsServiceImpl
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.util.UUID

class ContactsServiceTest {
    private fun buildNostrEvent(
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

    private fun buildPrimalQueryResult(
        subId: UUID = UUID.fromString("2F9F48CF-C62E-4BA9-AFA1-EF47C8FC146A"),
        primalEvents: List<PrimalEvent> = listOf(),
        nostrEvents: List<NostrEvent> = listOf()
    ) = PrimalQueryResult(
        primalEvents = primalEvents,
        terminationMessage = NostrIncomingMessage.EventMessage(subId),
        nostrEvents = nostrEvents
    )

    private fun buildUserAccount(
        pubkey: String = "test-pubkey",
        contactsCreatedAt: Long? = 0,
        notesCount: Int? = 420,
        followingCount: Int? = 1337,
        userDisplayName: String = "test-user-display-name",
        followersCount: Int? = 69,
        internetIdentifier: String? = "test@test.com",
        followers: List<String> = listOf("test1", "test2"),
        following: Set<String> = setOf("test1", "test2"),
        interests: List<String> = listOf("test1", "test2"),
        authorDisplayName: String = "test-author-display-name",
        pictureUrl: String? = "https://m.primal.net/HHPw.jpg",
        relays: List<Relay> = listOf(
            Relay(
                read = true,
                write = true,
                url = "wss://relay.primal.net"
            )
        )
    ) = UserAccount(
        pubkey = pubkey,
        contactsCreatedAt = contactsCreatedAt,
        notesCount = notesCount,
        followingCount = followingCount,
        userDisplayName = userDisplayName,
        followersCount = followersCount,
        internetIdentifier = internetIdentifier,
        followers = followers,
        following = following,
        interests = interests,
        authorDisplayName = authorDisplayName,
        pictureUrl = pictureUrl,
        relays = relays
    )

    private val newerContactsUserAccount = buildUserAccount(contactsCreatedAt = 2)
    private val olderContactsPrimalQueryResult = buildPrimalQueryResult(nostrEvents = listOf(buildNostrEvent(createdAt = 1)))

    private val newerContactsPrimalQueryResult = buildPrimalQueryResult(nostrEvents = listOf(buildNostrEvent(createdAt = 2)))
    private val olderContactsUserAccount = buildUserAccount(contactsCreatedAt = 1)

    private val missingContactsEventPrimalQueryResult = buildPrimalQueryResult()
    private val missingTagsInContactsEventPrimalQueryResult = buildPrimalQueryResult(nostrEvents = listOf(buildNostrEvent(tags = null)))

    @Test
    fun `contactsService should throw PrimalApiException for missing contacts event`() {
        val cs = ContactsServiceImpl()

        val exception = shouldThrow<PrimalApiException> {
            cs.prepareContacts(newerContactsUserAccount, missingContactsEventPrimalQueryResult)
        }

        exception.message shouldBe PrimalApiException.ContactListNotFound.message
    }

    @Test
    fun `contactsService should throw PrimalApiException for missing tags in contacts event`() {
        val cs = ContactsServiceImpl()

        val exception = shouldThrow<PrimalApiException> {
            cs.prepareContacts(
                newerContactsUserAccount,
                missingTagsInContactsEventPrimalQueryResult
            )
        }

        exception.message shouldBe PrimalApiException.ContactListTagsNotFound.message
    }

    @Test
    fun `contactsService should return cache server contacts when it's newer than local contacts list`() {
        val cs = ContactsServiceImpl()

        val actual = cs.prepareContacts(olderContactsUserAccount, newerContactsPrimalQueryResult)

        actual shouldBe newerContactsPrimalQueryResult.findNostrEvent(NostrEventKind.Contacts)?.tags?.parseFollowings()
    }

    @Test
    fun `contactsService should return local contacts when it's newer than cache server contacts list`() {
        val cs = ContactsServiceImpl()

        val actual = cs.prepareContacts(newerContactsUserAccount, olderContactsPrimalQueryResult)

        actual shouldBe newerContactsUserAccount.followers
    }
}