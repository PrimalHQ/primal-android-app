package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.updater.Updater
import net.primal.domain.events.EventRepository
import net.primal.domain.streams.StreamRepository

class UpdateStaleStreamDataUseCase(
    private val streamRepository: StreamRepository,
    private val eventRepository: EventRepository,
) : Updater() {
    override suspend fun doUpdate(): Result<Unit> {
        val staleStreamNaddrs = streamRepository.getStaleStreamNaddrs()
        if (staleStreamNaddrs.isEmpty()) return Result.success(Unit)

        return eventRepository.fetchReplaceableEvents(naddrs = staleStreamNaddrs)
    }
}
