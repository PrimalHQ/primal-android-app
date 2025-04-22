package net.primal.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.Napier
import javax.inject.Inject
import net.primal.android.core.crash.PrimalCrashReporter
import net.primal.android.core.images.PrimalImageLoaderFactory
import net.primal.android.core.utils.isGoogleBuild
import net.primal.core.config.store.AppConfigInitializer
import net.primal.data.repository.factory.PrimalRepositoryFactory
import timber.log.Timber

@HiltAndroidApp
class PrimalApp : Application() {

    @Inject
    lateinit var loggers: Set<@JvmSuppressWildcards Timber.Tree>

    @Inject
    lateinit var antilog: Set<@JvmSuppressWildcards Antilog>

    @Inject
    lateinit var imageLoaderFactory: PrimalImageLoaderFactory

    @Inject
    lateinit var crashReporter: PrimalCrashReporter

    override fun onCreate() {
        super.onCreate()
        AppConfigInitializer.init(this@PrimalApp)
        PrimalRepositoryFactory.init(this@PrimalApp)

        loggers.forEach {
            Timber.plant(it)
        }

        SingletonImageLoader.setSafe(imageLoaderFactory)
        antilog.firstOrNull()?.let { antilog ->
            Napier.base(antilog)
        }

        if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
            crashReporter.init()
        }

        if (isGoogleBuild()) {
            initNotificationChannels()
        }
    }

    private fun initNotificationChannels() {
        val defaultChannelId = "all_app_events"
        val defaultChannelName = getString(R.string.settings_notifications_channel_name_all_app_events)
        val channel = NotificationChannel(
            defaultChannelId,
            defaultChannelName,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
