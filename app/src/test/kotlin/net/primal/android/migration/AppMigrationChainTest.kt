package net.primal.android.migration

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldNotThrowAny
import org.junit.Test

class AppMigrationChainTest {

    private fun migration(start: Int, end: Int): AppMigration =
        object : AppMigration {
            override val startVersion = start
            override val endVersion = end
            override suspend fun migrate() = Unit
        }

    @Test
    fun `empty set with target zero is valid`() {
        shouldNotThrowAny {
            validateMigrationChain(migrations = emptySet(), targetVersion = 0)
        }
    }

    @Test
    fun `consecutive chain covering zero until target is valid`() {
        val chain = setOf(migration(0, 1), migration(1, 2), migration(2, 3))
        shouldNotThrowAny {
            validateMigrationChain(migrations = chain, targetVersion = 3)
        }
    }

    @Test
    fun `non consecutive migration is rejected`() {
        val chain = setOf(migration(0, 2))
        shouldThrow<IllegalArgumentException> {
            validateMigrationChain(migrations = chain, targetVersion = 2)
        }
    }

    @Test
    fun `gap in start versions is rejected`() {
        val chain = setOf(migration(0, 1), migration(2, 3))
        shouldThrow<IllegalArgumentException> {
            validateMigrationChain(migrations = chain, targetVersion = 3)
        }
    }

    @Test
    fun `gap in middle of covered range is rejected`() {
        // size == target (3), all consecutive, distinct starts [0,1,3], but version 2 is missing.
        val chain = setOf(migration(0, 1), migration(1, 2), migration(3, 4))
        shouldThrow<IllegalArgumentException> {
            validateMigrationChain(migrations = chain, targetVersion = 3)
        }
    }

    @Test
    fun `duplicate start version is rejected`() {
        // Two distinct instances share startVersion 0; set union keeps both (identity-based).
        val duplicated = setOf(migration(0, 1)) + setOf(migration(0, 1)) + setOf(migration(1, 2))
        shouldThrow<IllegalArgumentException> {
            validateMigrationChain(migrations = duplicated, targetVersion = 2)
        }
    }

    @Test
    fun `size not equal to target is rejected`() {
        val chain = setOf(migration(0, 1))
        shouldThrow<IllegalArgumentException> {
            validateMigrationChain(migrations = chain, targetVersion = 2)
        }
    }
}
