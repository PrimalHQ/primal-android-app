package net.primal.android.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.core.serialization.datastore.StringSerializer
import net.primal.android.test.MainDispatcherRule
import net.primal.android.test.advanceUntilIdleAndDelay
import net.primal.android.theme.active.ActiveThemeStore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ActiveThemeStoreTest {

    companion object {
        private const val DATA_STORE_FILE = "activeTheme"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val persistence: DataStore<String> = DataStoreFactory.create(
        serializer = StringSerializer(),
        produceFile = { testContext.dataStoreFile(DATA_STORE_FILE) }
    )

    @Test
    fun `initialValue is null`() {
        val activeThemeStore = ActiveThemeStore(persistence = persistence)
        val actual = activeThemeStore.userThemeState.value
        actual.shouldBeNull()
    }

    @Test
    fun `setUserTheme stores the user theme`() = runTest {
        val expectedTheme = net.primal.android.theme.domain.PrimalTheme.Sunset
        val activeThemeStore = ActiveThemeStore(persistence = persistence)
        activeThemeStore.setUserTheme(expectedTheme.themeName)
        advanceUntilIdleAndDelay()

        val actual = activeThemeStore.userThemeState.value
        actual.shouldNotBeNull()
        actual shouldBe expectedTheme
    }

    @Test
    fun `userThemeState is null if unknown theme is saved`() = runTest {
        persistence.updateData { "PrimalFutureTheme" }
        val activeThemeStore = ActiveThemeStore(persistence = persistence)
        advanceUntilIdle()

        val actual = activeThemeStore.userThemeState.value
        actual.shouldBeNull()
    }

    @Test
    fun `userThemeState corresponds to saved theme`() = runTest {
        val expectedTheme = net.primal.android.theme.domain.PrimalTheme.Sunrise
        persistence.updateData { expectedTheme.themeName }
        val activeThemeStore = ActiveThemeStore(persistence = persistence)
        advanceUntilIdle()

        val actual = activeThemeStore.userThemeState.value
        actual.shouldNotBeNull()
        actual shouldBe expectedTheme
    }
}
