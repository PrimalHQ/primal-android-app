package net.primal.android.migration

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.core.serialization.datastore.StringSerializer
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AppMigrationStoreTest {

    companion object {
        private const val DATA_STORE_FILE = "appMigrationVersionTest"
    }

    private val testContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val persistence: DataStore<String> = DataStoreFactory.create(
        serializer = StringSerializer(),
        produceFile = { testContext.dataStoreFile(DATA_STORE_FILE) },
    )

    private fun createStore() = AppMigrationStore(persistence = persistence)

    @Test
    fun `setVersion then currentVersion round trips`() = runTest {
        val store = createStore()
        store.setVersion(2)
        store.currentVersion() shouldBe 2
    }

    @Test
    fun `blank file reads as zero`() = runTest {
        persistence.updateData { "" }
        createStore().currentVersion() shouldBe 0
    }

    @Test
    fun `corrupt file reads as zero`() = runTest {
        persistence.updateData { "not-a-number" }
        createStore().currentVersion() shouldBe 0
    }
}
