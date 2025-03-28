package net.primal.data.remote.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.importing.PrimalImportApiImpl
import net.primal.domain.publisher.NostrEventImporter
import net.primal.domain.publisher.PrimalPublisher

object PublisherFactory {

    fun createNostrEventImporter(primalApiClient: PrimalApiClient): NostrEventImporter =
        PrimalImportApiImpl(primalApiClient)

    fun createPrimalPublisher(): PrimalPublisher = throw NotImplementedError()
}
