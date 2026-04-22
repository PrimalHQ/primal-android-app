package net.primal.data.local.dao.feeds

import net.primal.domain.feeds.FeedSpecKind

const val ALL_SPEC_KINDS_FILTER = "ALL"

fun FeedSpecKind?.asSpecKindFilter(): String = this?.name ?: ALL_SPEC_KINDS_FILTER
