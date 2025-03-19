package net.primal.android

import android.app.Application
import coil.Coil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import net.primal.android.core.crash.PrimalCrashReporter
import net.primal.android.core.images.PrimalImageLoaderFactory
import net.primal.core.config.store.AppConfigInitializer
import net.primal.core.init.PrimalInitializer
import timber.log.Timber

@HiltAndroidApp
class PrimalApp : Application() {

    @Inject
    lateinit var loggers: Set<@JvmSuppressWildcards Timber.Tree>

    @Inject
    lateinit var imageLoaderFactory: PrimalImageLoaderFactory

    @Inject
    lateinit var crashReporter: PrimalCrashReporter

    override fun onCreate() {
        super.onCreate()
        AppConfigInitializer.init(this@PrimalApp)
        PrimalInitializer.init(
            context = this@PrimalApp,
//            appName = "android-${BuildConfig.VERSION_NAME}",
//            userAgent = UserAgentProvider.USER_AGENT,
            showLog = true,
        )

        loggers.forEach {
            Timber.plant(it)
        }

        Coil.setImageLoader(imageLoaderFactory)

        if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
            crashReporter.init()
        }
    }
}
