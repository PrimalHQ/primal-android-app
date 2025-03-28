package net.primal.data.repository.mappers.remote

import net.primal.data.local.dao.explore.TrendingTopic
import net.primal.data.remote.api.explore.model.TopicScore

fun TopicScore.asTrendingTopicPO() = TrendingTopic(topic = this.name, score = this.score)
