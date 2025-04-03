package net.primal.android.core.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import net.primal.android.core.crash.PrimalCrashReporter
import net.primal.android.messages.security.Nip04MessageCipher
import net.primal.core.config.AppConfigFactory
import net.primal.core.config.AppConfigHandler
import net.primal.core.config.AppConfigProvider
import net.primal.domain.nostr.cryptography.MessageCipher
import okhttp3.OkHttpClient
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    /* Logging */
    @Provides
    @ElementsIntoSet
    fun emptyLoggersSet(): Set<Timber.Tree> = emptySet()

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver

    @Provides
    fun provideCrashReporter(okHttpClient: OkHttpClient) = PrimalCrashReporter(okHttpClient = okHttpClient)

    @Provides
    fun appConfigHandler(): AppConfigHandler = AppConfigFactory.createAppConfigHandler()

    @Provides
    fun appConfigProvider(): AppConfigProvider = AppConfigFactory.createAppConfigProvider()

    @Provides
    fun messagesCipher(nip04MessageCipher: Nip04MessageCipher): MessageCipher = nip04MessageCipher
}
