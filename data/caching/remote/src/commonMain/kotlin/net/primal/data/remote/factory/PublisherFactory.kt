package net.primal.data.remote.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.importing.PrimalImportApiImpl
import net.primal.domain.publisher.NostrEventImporter

object PublisherFactory {

    fun createNostrEventImporter(primalApiClient: PrimalApiClient): NostrEventImporter =
        PrimalImportApiImpl(primalApiClient)
}
