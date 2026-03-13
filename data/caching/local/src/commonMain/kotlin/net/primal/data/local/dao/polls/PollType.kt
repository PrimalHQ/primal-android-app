package net.primal.data.local.dao.polls

import net.primal.domain.polls.PollType as DomainPollType

enum class PollType {
    User,
    Zap,
}

fun PollType.asDO(): DomainPollType =
    when (this) {
        PollType.User -> DomainPollType.User
        PollType.Zap -> DomainPollType.Zap
    }
