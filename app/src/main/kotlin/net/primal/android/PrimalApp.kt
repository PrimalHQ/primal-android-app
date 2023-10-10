package net.primal.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import net.primal.android.core.crash.PrimalCrashReporter
import timber.log.Timber

@HiltAndroidApp
class PrimalApp : Application() {

    @Inject
    lateinit var loggers: Set<@JvmSuppressWildcards Timber.Tree>

    @Inject
    lateinit var crashReporter: PrimalCrashReporter

    override fun onCreate() {
        super.onCreate()
        loggers.forEach {
            Timber.plant(it)
        }

        if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
            crashReporter.init()
        }
    }
}
