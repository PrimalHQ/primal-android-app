package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.apps.local.LocalAppSessionEventData
import net.primal.domain.account.model.SessionEvent

expect fun LocalAppSessionEventData.asDomain(): SessionEvent?
