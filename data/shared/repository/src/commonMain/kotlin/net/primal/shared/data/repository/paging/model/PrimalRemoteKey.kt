package net.primal.shared.data.repository.paging.model

abstract class PrimalRemoteKey(
    open val sinceId: Long,
    open val untilId: Long,
    open val cachedAt: Long,
)
