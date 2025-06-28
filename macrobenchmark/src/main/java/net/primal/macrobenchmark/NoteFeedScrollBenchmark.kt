package net.primal.macrobenchmark

import android.content.ClipData
import android.content.ClipboardManager
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteFeedScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    companion object {
        private const val NOTE_FEED_LAZY_COLUMN = "noteFeedLazyColumn"
        private const val LAZY_LIST_PLACEHOLDER_TAG = "lazyListPlaceholder"
        private const val ITERATION_COUNT = 2
        private const val SCROLL_COUNT = 3
    }

    @Test
    fun loginAndScrollImagesFeed() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATION_COUNT,
            startupMode = StartupMode.WARM,
            setupBlock = {
                clearAllAppData()
                pressHome()
                startActivityAndWait()
                loginWith(key = BuildConfig.BENCHMARK_NSEC)
                waitForTagToBeGone(tag = LAZY_LIST_PLACEHOLDER_TAG)
            },
        ) {
            performScrollOnFirstScrollableComposable(scrollCount = SCROLL_COUNT, direction = Direction.DOWN)
        }

    @Test
    fun loginAndScrollVideosFeed() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATION_COUNT,
            startupMode = StartupMode.WARM,
            setupBlock = {
                clearAllAppData()
                pressHome()
                startActivityAndWait()
                loginWith(key = BuildConfig.BENCHMARK_NSEC)
                device.waitForIdle()
                performScrollOnFirstScrollableComposable(scrollCount = 1, direction = Direction.RIGHT)
                waitForTagToBeGone(tag = LAZY_LIST_PLACEHOLDER_TAG)
            },
        ) {
            performScrollOnFirstScrollableComposable(scrollCount = SCROLL_COUNT, direction = Direction.DOWN)
        }

    @Test
    fun loginAndScrollMixedFeed() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATION_COUNT,
            startupMode = StartupMode.WARM,
            setupBlock = {
                clearAllAppData()
                pressHome()
                startActivityAndWait()
                loginWith(key = BuildConfig.BENCHMARK_NSEC)
                device.waitForIdle()
                performScrollOnFirstScrollableComposable(scrollCount = 2, direction = Direction.RIGHT)
                waitForTagToBeGone(tag = LAZY_LIST_PLACEHOLDER_TAG)
            },
        ) {
            performScrollOnFirstScrollableComposable(scrollCount = SCROLL_COUNT, direction = Direction.DOWN)
        }

    private fun MacrobenchmarkScope.waitForTagToBeGone(tag: String) {
        device.wait(Until.gone(By.res(tag)), DEFAULT_TIMEOUT)
    }

    private fun MacrobenchmarkScope.performScrollOnFirstScrollableComposable(scrollCount: Int, direction: Direction) {
        var list = device.wait(
            Until.findObject(By.res(NOTE_FEED_LAZY_COLUMN)),
            DEFAULT_TIMEOUT,
        ) ?: error("Couldn't locate NoteFeedLazyColumn on screen.")

        repeat(scrollCount) {
            try {
                list.fling(direction)
            } catch (_: StaleObjectException) {
                list = device.findObject(By.res(NOTE_FEED_LAZY_COLUMN))
                list.fling(direction)
            }

            device.waitForIdle(DEFAULT_TIMEOUT)
        }
    }

    private fun MacrobenchmarkScope.clearAllAppData() = device.executeShellCommand("pm clear $packageName")

    private fun MacrobenchmarkScope.loginWith(key: String) {
        require(key.isNotEmpty()) { "npub/nsec is empty." }

        val welcomeSignInButtonRes = "welcomeSignInButton"
        val loginSignInButtonRes = "loginSignInButton"

        val inst = InstrumentationRegistry.getInstrumentation()
        val clipboard = inst.targetContext
            .getSystemService(ClipboardManager::class.java)
            ?: error("Couldn't get clipboard manager.")

        val clip = ClipData.newPlainText("key", key)
        clipboard.setPrimaryClip(clip)

        device.wait(Until.hasObject(By.res(welcomeSignInButtonRes)), DEFAULT_TIMEOUT)
        device.findObject(By.res(welcomeSignInButtonRes)).click()

        device.waitForIdle()

        device.wait(Until.hasObject(By.res(loginSignInButtonRes)), DEFAULT_TIMEOUT)
        device.findObjects(By.res(loginSignInButtonRes)).lastOrNull()?.click()
    }
}
