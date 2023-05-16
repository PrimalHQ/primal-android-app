package net.primal.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class PrimalApp : Application() {

    @Inject
    lateinit var loggers: Set<@JvmSuppressWildcards Timber.Tree>

    override fun onCreate() {
        super.onCreate()
        loggers.forEach {
            Timber.plant(it)
        }
    }

}
