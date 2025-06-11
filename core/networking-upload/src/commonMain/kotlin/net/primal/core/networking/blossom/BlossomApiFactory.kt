package net.primal.core.networking.blossom

import net.primal.core.utils.coroutines.createDispatcherProvider

object BlossomApiFactory {

    fun create(baseBlossomUrl: String): BlossomApi {
        return BlossomApiImpl(
            dispatcherProvider = createDispatcherProvider(),
            baseBlossomUrl = baseBlossomUrl,
        )
    }
}
