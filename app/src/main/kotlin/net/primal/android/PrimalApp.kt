package net.primal.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import coil3.SingletonImageLoader
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.Napier
import javax.inject.Inject
import net.primal.android.core.crash.PrimalCrashReporter
import net.primal.android.core.images.PrimalImageLoaderFactory
import net.primal.android.core.utils.isGoogleBuild
import net.primal.android.wallet.init.TsunamiWalletLifecycleInitializer
import net.primal.core.config.store.AppConfigInitializer
import net.primal.data.account.repository.repository.factory.AccountRepositoryFactory
import net.primal.data.repository.factory.PrimalRepositoryFactory
import net.primal.wallet.data.repository.factory.WalletRepositoryFactory

@HiltAndroidApp
class PrimalApp : Application() {

    @Inject
    lateinit var antilog: Set<@JvmSuppressWildcards Antilog>

    @Inject
    lateinit var imageLoaderFactory: PrimalImageLoaderFactory

    @Inject
    lateinit var crashReporter: PrimalCrashReporter

    @Inject
    lateinit var tsunamiWalletLifecycleInitializer: Lazy<TsunamiWalletLifecycleInitializer>

    override fun onCreate() {
        super.onCreate()
        AppConfigInitializer.init(this@PrimalApp)
        PrimalRepositoryFactory.init(this@PrimalApp)
        WalletRepositoryFactory.init(context = this@PrimalApp, enableDbEncryption = !BuildConfig.DEBUG)
        AccountRepositoryFactory.init(context = this@PrimalApp, enableDbEncryption = !BuildConfig.DEBUG)

        SingletonImageLoader.setSafe(imageLoaderFactory)
        antilog.forEach { Napier.base(it) }

        if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
            crashReporter.init()
        }

        if (isGoogleBuild()) {
            initNotificationChannels()
        }

        tsunamiWalletLifecycleInitializer.get().start()
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
