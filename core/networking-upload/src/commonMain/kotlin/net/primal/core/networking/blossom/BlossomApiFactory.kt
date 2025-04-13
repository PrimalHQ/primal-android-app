package net.primal.core.networking.blossom

import net.primal.core.utils.coroutines.DispatcherProviderFactory

object BlossomApiFactory {

    fun create(baseBlossomUrl: String): BlossomApi {
        return BlossomApiImpl(
            dispatcherProvider = DispatcherProviderFactory.create(),
            baseBlossomUrl = baseBlossomUrl,
        )
    }
}
