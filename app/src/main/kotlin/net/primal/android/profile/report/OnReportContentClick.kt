package net.primal.android.profile.report

import net.primal.domain.nostr.ReportType

typealias OnReportContentClick = (type: ReportType, profileId: String, noteId: String) -> Unit
