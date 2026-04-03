package net.primal.data.repository.nip05

import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import net.primal.data.local.dao.profiles.Nip05VerificationData
import net.primal.data.local.dao.profiles.Nip05VerificationDataDao
import net.primal.domain.profile.Nip05VerificationStatus

@Suppress("MaxLineLength")
class Nip05VerificationServiceTest {

    private val testPubkey = "abc123pubkey"
    private val testIdentifier = "alice@example.com"

    private fun createMockEngine(
        responseBody: String = """{"names":{"alice":"$testPubkey"}}""",
        status: HttpStatusCode = HttpStatusCode.OK,
    ): MockEngine =
        MockEngine { _ ->
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

    private fun createHttpClient(engine: MockEngine): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
        }

    private fun createService(
        engine: MockEngine = createMockEngine(),
        cachedData: Nip05VerificationData? = null,
        existingRecords: List<Nip05VerificationData> = emptyList(),
    ): Pair<Nip05VerificationServiceImpl, Nip05VerificationDataDao> {
        val dao = mockk<Nip05VerificationDataDao>(relaxUnitFun = true)
        coEvery { dao.find(any()) } returns cachedData
        coEvery { dao.findAll(any()) } returns existingRecords
        val client = createHttpClient(engine)
        return Nip05VerificationServiceImpl(httpClient = client, verificationDao = dao) to dao
    }

    @Test
    fun `verifyIfNeeded sets VERIFIED when pubkey matches`() =
        runTest {
            val (service, dao) = createService()
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.VERIFIED
            slot.captured.ownerId shouldBe testPubkey
            slot.captured.verifiedAddress shouldBe testIdentifier
        }

    @Test
    fun `verifyIfNeeded sets FAILED when pubkey does not match`() =
        runTest {
            val engine = createMockEngine(responseBody = """{"names":{"alice":"different_pubkey"}}""")
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.FAILED
        }

    @Test
    fun `verifyIfNeeded sets ERROR on HTTP failure`() =
        runTest {
            val engine = createMockEngine(status = HttpStatusCode.InternalServerError)
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.ERROR
        }

    @Test
    fun `verifyIfNeeded sets FAILED for invalid identifier format`() =
        runTest {
            val (service, dao) = createService()
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = "no-at-sign")

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.FAILED
        }

    @Test
    fun `verifyIfNeeded skips verification when cache is fresh`() =
        runTest {
            val freshCachedData = Nip05VerificationData(
                ownerId = testPubkey,
                status = Nip05VerificationStatus.VERIFIED,
                lastCheckedAt = kotlin.time.Clock.System.now().epochSeconds,
                verifiedAddress = testIdentifier,
            )
            val (service, dao) = createService(cachedData = freshCachedData)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            coVerify(exactly = 0) { dao.upsert(any()) }
        }

    @Test
    fun `verifyIfNeeded re-verifies when cache is expired`() =
        runTest {
            val expiredCachedData = Nip05VerificationData(
                ownerId = testPubkey,
                status = Nip05VerificationStatus.VERIFIED,
                lastCheckedAt = 0L,
                verifiedAddress = testIdentifier,
            )
            val (service, dao) = createService(cachedData = expiredCachedData)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            coVerify(atLeast = 1) { dao.upsert(any()) }
        }

    @Test
    fun `verifyIfNeeded does not overwrite VERIFIED with ERROR`() =
        runTest {
            val engine = createMockEngine(status = HttpStatusCode.InternalServerError)
            val verifiedData = Nip05VerificationData(
                ownerId = testPubkey,
                status = Nip05VerificationStatus.VERIFIED,
                lastCheckedAt = kotlin.time.Clock.System.now().epochSeconds,
                verifiedAddress = testIdentifier,
            )
            val (service, dao) = createService(engine = engine, cachedData = verifiedData)
            service.verifyEagerly(pubkey = testPubkey, internetIdentifier = testIdentifier)

            coVerify(exactly = 0) { dao.upsert(any()) }
        }

    @Test
    fun `verifyIfNeeded re-verifies when identifier changes`() =
        runTest {
            val engine = createMockEngine(responseBody = """{"names":{"bob":"$testPubkey"}}""")
            val cachedWithOldAddress = Nip05VerificationData(
                ownerId = testPubkey,
                status = Nip05VerificationStatus.VERIFIED,
                lastCheckedAt = kotlin.time.Clock.System.now().epochSeconds,
                verifiedAddress = "old@example.com",
            )
            val (service, dao) = createService(engine = engine, cachedData = cachedWithOldAddress)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = "bob@example.com")

            coVerify(atLeast = 1) { dao.upsert(any()) }
        }

    @Test
    fun `verifies when input has mixed case and response has lowercase name`() =
        runTest {
            // Real-world case: nostrly.com returns all-lowercase names regardless of query casing
            val engine = createMockEngine(responseBody = """{"names":{"unclejim21":"$testPubkey"}}""")
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = "uncleJim21@nostrly.com")

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.VERIFIED
        }

    @Test
    fun `verifies when input has lowercase and response has mixed case name`() =
        runTest {
            val engine = createMockEngine(responseBody = """{"names":{"Alice":"$testPubkey"}}""")
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = "alice@example.com")

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.VERIFIED
        }

    @Test
    fun `verifies when input has uppercase and response has lowercase name`() =
        runTest {
            val engine = createMockEngine(responseBody = """{"names":{"alice":"$testPubkey"}}""")
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = "Alice@example.com")

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.VERIFIED
        }

    @Test
    fun `verifies when both input and response have different mixed casing`() =
        runTest {
            val engine = createMockEngine(responseBody = """{"names":{"ALICE":"$testPubkey"}}""")
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = "aLiCe@example.com")

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.VERIFIED
        }

    @Test
    fun `verifyIfNeeded sets ERROR on network exception`() =
        runTest {
            val engine = MockEngine { _ ->
                throw java.io.IOException("Network failure")
            }
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.ERROR
        }

    @Test
    fun `verifyIfNeeded sets FAILED when name not in response`() =
        runTest {
            val engine = createMockEngine(responseBody = """{"names":{"bob":"some_other_pubkey"}}""")
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.FAILED
        }

    @Test
    fun `optimistically verifies other names from response`() =
        runTest {
            val bobPubkey = "bob_pubkey_123"
            val engine = createMockEngine(
                responseBody = """{"names":{"alice":"$testPubkey","bob":"$bobPubkey"}}""",
            )
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.ownerId shouldBe testPubkey
            slot.captured.status shouldBe Nip05VerificationStatus.VERIFIED

            val batchSlot = slot<List<Nip05VerificationData>>()
            coVerify { dao.upsertAll(capture(batchSlot)) }
            batchSlot.captured.size shouldBe 1
            batchSlot.captured.first().ownerId shouldBe bobPubkey
            batchSlot.captured.first().status shouldBe Nip05VerificationStatus.VERIFIED
            batchSlot.captured.first().verifiedAddress shouldBe "bob@example.com"
        }

    @Test
    fun `optimistic verifications happen even when primary verification fails`() =
        runTest {
            val alicePubkey = "different_pubkey"
            val bobPubkey = "bob_pubkey"
            val engine = createMockEngine(
                responseBody = """{"names":{"alice":"$alicePubkey","bob":"$bobPubkey"}}""",
            )
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            val slot = slot<Nip05VerificationData>()
            coVerify { dao.upsert(capture(slot)) }
            slot.captured.status shouldBe Nip05VerificationStatus.FAILED

            val batchSlot = slot<List<Nip05VerificationData>>()
            coVerify { dao.upsertAll(capture(batchSlot)) }
            batchSlot.captured.size shouldBe 2
            batchSlot.captured.map { it.ownerId }.toSet() shouldBe setOf(alicePubkey, bobPubkey)
            batchSlot.captured.all { it.status == Nip05VerificationStatus.VERIFIED } shouldBe true
        }

    @Test
    fun `no optimistic verifications on error`() =
        runTest {
            val engine = createMockEngine(status = HttpStatusCode.InternalServerError)
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            coVerify(exactly = 0) { dao.upsertAll(any()) }
        }

    @Test
    fun `optimistic verification with multiple extra names`() =
        runTest {
            val bobPubkey = "bob_pubkey"
            val carolPubkey = "carol_pubkey"
            val engine = createMockEngine(
                responseBody = """{"names":{"alice":"$testPubkey","bob":"$bobPubkey","carol":"$carolPubkey"}}""",
            )
            val (service, dao) = createService(engine = engine)
            service.verifyIfNeeded(pubkey = testPubkey, internetIdentifier = testIdentifier)

            val batchSlot = slot<List<Nip05VerificationData>>()
            coVerify { dao.upsertAll(capture(batchSlot)) }
            batchSlot.captured.size shouldBe 2
            batchSlot.captured.map { it.ownerId }.toSet() shouldBe setOf(bobPubkey, carolPubkey)
            batchSlot.captured.all { it.status == Nip05VerificationStatus.VERIFIED } shouldBe true
        }

    @Test
    fun `does not overwrite FAILED with ERROR`() =
        runTest {
            val engine = createMockEngine(status = HttpStatusCode.InternalServerError)
            val failedData = Nip05VerificationData(
                ownerId = testPubkey,
                status = Nip05VerificationStatus.FAILED,
                lastCheckedAt = kotlin.time.Clock.System.now().epochSeconds,
                verifiedAddress = testIdentifier,
            )
            val (service, dao) = createService(engine = engine, cachedData = failedData)
            service.verifyEagerly(pubkey = testPubkey, internetIdentifier = testIdentifier)

            coVerify(exactly = 0) { dao.upsert(any()) }
        }
}
