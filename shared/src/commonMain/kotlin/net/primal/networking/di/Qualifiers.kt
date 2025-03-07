package net.primal.networking.di

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue


object PrimalCacheApiClient : Qualifier {
    override val value: QualifierValue = "PrimalCacheApiClient"
}

object PrimalUploadApiClient : Qualifier {
    override val value: QualifierValue = "PrimalUploadApiClient"
}

object PrimalWalletApiClient : Qualifier {
    override val value: QualifierValue = "PrimalWalletApiClient"
}

object WebSocketHttpClient : Qualifier {
    override val value: QualifierValue = "WebSocketHttpClient"
}

object RegularHttpClient : Qualifier {
    override val value: QualifierValue = "RegularHttpClient"
}
