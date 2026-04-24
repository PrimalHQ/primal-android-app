package net.primal.data.local.db

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private const val CHUNK_SIZE = 3

class ChunkedInClauseTest {

    @Test
    fun `chunkedQuery short-circuits for empty list`() =
        runTest {
            val invocations = mutableListOf<List<String>>()
            val result = emptyList<String>().chunkedQuery(chunkSize = CHUNK_SIZE) { chunk ->
                invocations += chunk
                chunk
            }

            result shouldBe emptyList()
            invocations shouldBe listOf(emptyList())
        }

    @Test
    fun `chunkedQuery short-circuits when size is at or below chunk size`() =
        runTest {
            val input = (1..CHUNK_SIZE).map { "id_$it" }
            val invocations = mutableListOf<List<String>>()

            val result = input.chunkedQuery(chunkSize = CHUNK_SIZE) { chunk ->
                invocations += chunk
                chunk
            }

            result shouldBe input
            invocations.size shouldBe 1
            invocations.single() shouldBe input
        }

    @Test
    fun `chunkedQuery splits and concatenates when size exceeds chunk size`() =
        runTest {
            val input = (1..(2 * CHUNK_SIZE + 1)).map { "id_$it" }
            val invocations = mutableListOf<List<String>>()

            val result = input.chunkedQuery(chunkSize = CHUNK_SIZE) { chunk ->
                invocations += chunk
                chunk
            }

            invocations.size shouldBe 3
            invocations[0].size shouldBe CHUNK_SIZE
            invocations[1].size shouldBe CHUNK_SIZE
            invocations[2].size shouldBe 1
            result shouldBe input
        }

    @Test
    fun `chunkedMapQuery short-circuits when size is at or below chunk size`() =
        runTest {
            val input = (1..CHUNK_SIZE).map { "id_$it" }
            val invocations = mutableListOf<List<String>>()

            val result = input.chunkedMapQuery(chunkSize = CHUNK_SIZE) { chunk ->
                invocations += chunk
                chunk.associateWith { it.uppercase() }
            }

            invocations.size shouldBe 1
            result.size shouldBe CHUNK_SIZE
            result["id_1"] shouldBe "ID_1"
        }

    @Test
    fun `chunkedMapQuery merges map entries across chunks`() =
        runTest {
            val inputSize = 2 * CHUNK_SIZE + 1
            val input = (1..inputSize).map { "id_$it" }

            val result = input.chunkedMapQuery(chunkSize = CHUNK_SIZE) { chunk ->
                chunk.associateWith { it.uppercase() }
            }

            result.size shouldBe inputSize
            result["id_1"] shouldBe "ID_1"
            result["id_$inputSize"] shouldBe "ID_$inputSize"
        }

    @Test
    fun `chunkedFlowQuery short-circuits when size is at or below chunk size`() =
        runTest {
            val input = (1..CHUNK_SIZE).map { "id_$it" }
            val invocations = mutableListOf<List<String>>()

            val flow = input.chunkedFlowQuery(chunkSize = CHUNK_SIZE) { chunk ->
                invocations += chunk
                kotlinx.coroutines.flow.flowOf(chunk)
            }

            flow.first() shouldBe input
            invocations.size shouldBe 1
        }

    @Test
    fun `chunkedFlowQuery combines initial emissions across chunks`() =
        runTest {
            val input = (1..(2 * CHUNK_SIZE)).map { "id_$it" }
            val stateFlows = mutableListOf<kotlinx.coroutines.flow.MutableStateFlow<List<String>>>()

            val flow = input.chunkedFlowQuery(chunkSize = CHUNK_SIZE) { chunk ->
                val state = kotlinx.coroutines.flow.MutableStateFlow(chunk)
                stateFlows += state
                state
            }

            val first = flow.first()

            stateFlows.size shouldBe 2
            first shouldBe input
        }

    @Test
    fun `chunkedFlowQuery re-emits when a chunk updates`() =
        runTest {
            val input = (1..(2 * CHUNK_SIZE)).map { "id_$it" }
            val stateFlows = mutableListOf<kotlinx.coroutines.flow.MutableStateFlow<List<String>>>()

            val flow = input.chunkedFlowQuery(chunkSize = CHUNK_SIZE) { chunk ->
                val state = kotlinx.coroutines.flow.MutableStateFlow(chunk)
                stateFlows += state
                state
            }

            val collected = mutableListOf<List<String>>()
            val job = launch {
                flow.collect { collected += it }
            }

            runCurrent()
            stateFlows[0].value = listOf("replacement")
            runCurrent()
            job.cancel()

            collected.size shouldBe 2
            collected[0] shouldBe input
            collected[1] shouldBe listOf("replacement") + input.drop(CHUNK_SIZE)
        }
}
