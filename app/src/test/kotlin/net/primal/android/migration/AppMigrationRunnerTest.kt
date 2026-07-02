package net.primal.android.migration

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class AppMigrationRunnerTest {

    private fun storeAt(version: Int) =
        AppMigrationStore(persistence = InMemoryStringDataStore(version.toString()))

    private fun runner(
        migrations: Set<AppMigration>,
        store: AppMigrationStore,
        target: Int,
    ) = AppMigrationRunner(migrations = migrations, versionStore = store, targetVersion = target)

    @Test
    fun `happy path runs all migrations in order and persists target`() = runTest {
        val order = mutableListOf<Int>()
        val m0 = FakeAppMigration(0, 1) { order += 0 }
        val m1 = FakeAppMigration(1, 2) { order += 1 }
        val m2 = FakeAppMigration(2, 3) { order += 2 }
        val store = storeAt(0)

        runner(setOf(m0, m1, m2), store, target = 3).runPendingMigrations()

        order shouldBe listOf(0, 1, 2)
        store.currentVersion() shouldBe 3
    }

    @Test
    fun `version skip runs only pending migrations`() = runTest {
        val m0 = FakeAppMigration(0, 1)
        val m1 = FakeAppMigration(1, 2)
        val m2 = FakeAppMigration(2, 3)
        val store = storeAt(1)

        runner(setOf(m0, m1, m2), store, target = 3).runPendingMigrations()

        m0.runCount shouldBe 0
        m1.runCount shouldBe 1
        m2.runCount shouldBe 1
        store.currentVersion() shouldBe 3
    }

    @Test
    fun `already current runs nothing`() = runTest {
        val m0 = FakeAppMigration(0, 1)
        val m1 = FakeAppMigration(1, 2)
        val m2 = FakeAppMigration(2, 3)
        val store = storeAt(3)

        runner(setOf(m0, m1, m2), store, target = 3).runPendingMigrations()

        m0.runCount shouldBe 0
        m1.runCount shouldBe 0
        m2.runCount shouldBe 0
        store.currentVersion() shouldBe 3
    }

    @Test
    fun `empty set with target zero is a no-op`() = runTest {
        val store = storeAt(0)
        runner(emptySet(), store, target = 0).runPendingMigrations()
        store.currentVersion() shouldBe 0
    }

    @Test
    fun `mid chain failure commits prior step, stops, and resumes next launch`() = runTest {
        val m0 = FakeAppMigration(0, 1)
        var m1ShouldFail = true
        val m1 = FakeAppMigration(1, 2) { if (m1ShouldFail) error("boom") }
        val m2 = FakeAppMigration(2, 3)
        val store = storeAt(0)
        val migrations = setOf(m0, m1, m2)

        // First launch: 0->1 commits, 1->2 fails, chain stops, returns normally.
        runner(migrations, store, target = 3).runPendingMigrations()
        m0.runCount shouldBe 1
        m1.runCount shouldBe 1
        m2.runCount shouldBe 0
        store.currentVersion() shouldBe 1

        // Next launch: transient failure gone, resumes from 1.
        m1ShouldFail = false
        runner(migrations, store, target = 3).runPendingMigrations()
        m0.runCount shouldBe 1 // not re-run
        m1.runCount shouldBe 2
        m2.runCount shouldBe 1
        store.currentVersion() shouldBe 3
    }

    @Test
    fun `missing edge stops and returns without throwing`() = runTest {
        // target 2 but no migration registered from version 1
        val m0 = FakeAppMigration(0, 1)
        val store = storeAt(0)

        runner(setOf(m0), store, target = 2).runPendingMigrations()

        m0.runCount shouldBe 1
        store.currentVersion() shouldBe 1 // advanced to 1, then stopped at missing 1->2
    }

    @Test
    fun `store read failure does not throw out of runner`() = runTest {
        val throwingStore = AppMigrationStore(
            persistence = object : androidx.datastore.core.DataStore<String> {
                override val data = flow<String> { throw IOException("read boom") }
                override suspend fun updateData(transform: suspend (String) -> String): String =
                    error("should not be called")
            },
        )
        val m0 = FakeAppMigration(0, 1)
        shouldNotThrowAny {
            runner(setOf(m0), throwingStore, target = 1).runPendingMigrations()
        }
        m0.runCount shouldBe 0 // never got past the failing initial read
    }

    @Test
    fun `store write failure does not throw out of runner`() = runTest {
        val store = AppMigrationStore(
            persistence = object : androidx.datastore.core.DataStore<String> {
                override val data = flowOf("0")
                override suspend fun updateData(transform: suspend (String) -> String): String =
                    throw IOException("write boom")
            },
        )
        val m0 = FakeAppMigration(0, 1)
        shouldNotThrowAny {
            runner(setOf(m0), store, target = 1).runPendingMigrations()
        }
        m0.runCount shouldBe 1 // migrate ran; only the persist failed, and it was swallowed
    }
}
