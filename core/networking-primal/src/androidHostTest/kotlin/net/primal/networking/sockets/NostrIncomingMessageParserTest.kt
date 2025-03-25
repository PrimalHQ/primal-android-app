package net.primal.networking.sockets

import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.parseIncomingMessage
import net.primal.domain.nostr.NostrEvent

class NostrIncomingMessageParserTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @org.junit.Test
    fun `EVENT message parsed as EventMessage`() {
        val jsonMessage = buildJsonArray {
            add("EVENT")
            add(Uuid.random().toString())
            add(
                json.encodeToJsonElement(
                    NostrEvent(
                        id = "invalidId",
                        pubKey = "pukey",
                        createdAt = Clock.System.now().epochSeconds,
                        kind = 1,
                        tags = emptyList(),
                        content = "",
                        sig = "signature",
                    ),
                ),
            )
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.EventMessage>()
    }

    @org.junit.Test
    fun `EOSE message parsed as EoseMessage`() {
        val jsonMessage = buildJsonArray {
            add("EOSE")
            add(Uuid.random().toString())
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.EoseMessage>()
    }

    @org.junit.Test
    fun `NOTICE message parsed as NoticeMessage`() {
        val jsonMessage = buildJsonArray {
            add("NOTICE")
            add("Your pubkey is not registered.")
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.NoticeMessage>()
    }

    @org.junit.Test
    fun `OK false message parsed as OkMessage`() {
        val jsonMessage = buildJsonArray {
            add("OK")
            add("ThisIsEventId")
            add(false)
            add("blocked: pubkey not registered.")
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.OkMessage>()
    }

    @org.junit.Test
    fun `OK true message parsed as OkMessage`() {
        val jsonMessage = buildJsonArray {
            add("OK")
            add("ThisIsEventId")
            add(true)
            add("")
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.OkMessage>()
    }

    @org.junit.Test
    fun `AUTH message parsed as AuthMessage`() {
        val jsonMessage = buildJsonArray {
            add("AUTH")
            add("AuthChallenge")
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.AuthMessage>()
    }

    @org.junit.Test
    fun `COUNT message parsed as CountMessage`() {
        val jsonMessage = buildJsonArray {
            add("COUNT")
            add(Uuid.random().toString())
            add(
                buildJsonObject {
                    put("count", 123)
                },
            )
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.CountMessage>()
    }
}
