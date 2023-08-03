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
    private fun buildPrimalQueryResult(
        subId: UUID,
        primalEvents: List<PrimalEvent> = listOf(),
        nostrEvents: List<NostrEvent> = listOf()
    ): PrimalQueryResult {
        return PrimalQueryResult(
            primalEvents = primalEvents,
            terminationMessage = NostrIncomingMessage.EventMessage(subId),
            nostrEvents = nostrEvents
        )
    }

    private val happyPathPrimalQueryResult = buildPrimalQueryResult(
        UUID.fromString("2F9F48CF-C62E-4BA9-AFA1-EF47C8FC146A"), nostrEvents = listOf(
            NostrEvent(
                pubKey = "test-pubkey",
                content = "",
                createdAt = 123,
                id = "test-id",
                kind = NostrEventKind.Contacts.value,
                sig = "test-sig",
                tags = listOf()
            )
        )
    )

    private val missingContactsEventPrimalQueryResult = buildPrimalQueryResult(
        UUID.fromString("2F9F48CF-C62E-4BA9-AFA1-EF47C8FC146A")
    )

    private val missingTagsInContactsEventPrimalQueryResult = buildPrimalQueryResult(
        UUID.fromString("2F9F48CF-C62E-4BA9-AFA1-EF47C8FC146A"), nostrEvents = listOf(
            NostrEvent(
                pubKey = "test-pubkey",
                content = "",
                createdAt = 123,
                id = "test-id",
                kind = NostrEventKind.Contacts.value,
                sig = "test-sig",
                tags = null
            )
        )
    )

    private val happyPathUserAccount: UserAccount = UserAccount(
        pubkey = "test-pubkey",
        contactsCreatedAt = 124,
        notesCount = 420,
        followingCount = 1337,
        userDisplayName = "test-user-display-name",
        followersCount = 69,
        internetIdentifier = "test@test.com",
        followers = listOf("test1", "test2"),
        following = setOf("test1", "test2"),
        interests = listOf("test1", "test2"),
        authorDisplayName = "test-author-display-name",
        pictureUrl = "https://m.primal.net/HHPw.jpg",
        relays = listOf(Relay(read = true, write = true, url = "wss://relay.primal.net"))
    )


    @Test
    fun `contactsService happy path passes without fail`() {
        val cs = ContactsServiceImpl()

        val actual = cs.prepareContacts(happyPathUserAccount, happyPathPrimalQueryResult)

        actual shouldBe happyPathUserAccount.followers
    }

    @Test
    fun `contactsService should throw PrimalApiException for missing contacts event`() {
        val cs = ContactsServiceImpl()

        val exception = shouldThrow<PrimalApiException> {
            cs.prepareContacts(happyPathUserAccount, missingContactsEventPrimalQueryResult)
        }

        exception.message shouldBe PrimalApiException.ContactListNotFound.message
    }

    @Test
    fun `contactsService should throw PrimalApiException for missing tags in contacts event`() {
        val cs = ContactsServiceImpl()

        val exception = shouldThrow<PrimalApiException> {
            cs.prepareContacts(happyPathUserAccount, missingTagsInContactsEventPrimalQueryResult)
        }

        exception.message shouldBe PrimalApiException.ContactListTagsNotFound.message
    }

    @Test
    fun `contactsService should return cache server contacts when it's newer than local contacts list`() {

    }

    @Test
    fun `contactsService should return local contacts when it's newer than cache server contacts list`() {

    }
}