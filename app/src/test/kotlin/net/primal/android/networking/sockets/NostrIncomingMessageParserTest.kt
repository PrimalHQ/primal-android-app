package net.primal.android.networking.sockets

import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.core.serialization.json.NostrJson
import org.junit.Test
import java.time.Instant
import java.util.UUID

class NostrIncomingMessageParserTest {

    @Test
    fun `EVENT message parsed as EventMessage`() {
        val jsonMessage = buildJsonArray {
            add("EVENT")
            add(UUID.randomUUID().toString())
            add(
                NostrJson.encodeToJsonElement(
                    NostrEvent(
                        id = "invalidId",
                        pubKey = "pukey",
                        createdAt = Instant.now().epochSecond,
                        kind = 1,
                        tags = emptyList(),
                        content = "",
                        sig = "signature",
                    )
                )
            )
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.EventMessage>()
    }

    @Test
    fun `EOSE message parsed as EoseMessage`() {
        val jsonMessage = buildJsonArray {
            add("EOSE")
            add(UUID.randomUUID().toString())
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.EoseMessage>()
    }

    @Test
    fun `NOTICE message parsed as NoticeMessage`() {
        val jsonMessage = buildJsonArray {
            add("NOTICE")
            add("Your pubkey is not registered.")
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.NoticeMessage>()
    }

    @Test
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

    @Test
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

    @Test
    fun `AUTH message parsed as AuthMessage`() {
        val jsonMessage = buildJsonArray {
            add("AUTH")
            add("AuthChallenge")
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.AuthMessage>()
    }

    @Test
    fun `COUNT message parsed as CountMessage`() {
        val jsonMessage = buildJsonArray {
            add("COUNT")
            add(UUID.randomUUID().toString())
            add(buildJsonObject {
                put("count", 123)
            })
        }

        val actual = jsonMessage.toString().parseIncomingMessage()
        actual should beInstanceOf<NostrIncomingMessage.CountMessage>()
    }
}
