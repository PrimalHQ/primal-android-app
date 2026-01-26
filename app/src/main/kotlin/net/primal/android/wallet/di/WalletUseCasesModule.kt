package net.primal.android.wallet.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.builder.TxRequestBuilder
import net.primal.domain.usecase.ConnectNwcUseCase
import net.primal.domain.usecase.CreateSparkWalletUseCase
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.builder.factory.TxRequestBuilderFactory

@Module
@InstallIn(SingletonComponent::class)
object WalletUseCasesModule {

    @Provides
    @Singleton
    fun providesTxRequestBuilder(): TxRequestBuilder = TxRequestBuilderFactory.createTxRequestBuilder()

    @Provides
    @Singleton
    fun providesConnectNwcUseCase(
        walletRepository: WalletRepository,
        walletAccountRepository: WalletAccountRepository,
    ): ConnectNwcUseCase =
        ConnectNwcUseCase(
            walletRepository = walletRepository,
            walletAccountRepository = walletAccountRepository,
        )

    @Provides
    @Singleton
    fun providesCreateTsunamiWalletUseCase(
        sparkWalletAccountRepository: SparkWalletAccountRepository,
    ): CreateSparkWalletUseCase =
        CreateSparkWalletUseCase(
            sparkWalletAccountRepository = sparkWalletAccountRepository,
        )
}
