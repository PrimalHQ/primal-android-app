package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.explore.TrendingTopic
import net.primal.domain.explore.ExploreTrendingTopic

fun TrendingTopic.asExploreTrendingTopic(): ExploreTrendingTopic {
    return ExploreTrendingTopic(
        topic = this.topic,
        score = this.score,
    )
}
