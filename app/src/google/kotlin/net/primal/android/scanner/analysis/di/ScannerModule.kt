package net.primal.android.scanner.analysis.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.scanner.analysis.GoogleBarcodeScanner
import net.primal.android.scanner.analysis.QrCodeResultDecoder

@Module
@InstallIn(SingletonComponent::class)
abstract class ScannerModule {
    @Binds
    abstract fun provideTokenUpdater(updater: GoogleBarcodeScanner): QrCodeResultDecoder
}
