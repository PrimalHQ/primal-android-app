package net.primal.data.repository.utils

import net.primal.domain.nostr.getTagValueOrNull
import net.primal.domain.nostr.hasReplyMarker
import net.primal.domain.nostr.hasRootMarker
import net.primal.domain.posts.FeedPost

/**
 * Tries to perform topological sort calling [performTopologicalSort].  In case the sort fails,
 * returns the original list.
 */
fun List<FeedPost>.performTopologicalSortOrThis() = runCatching { performTopologicalSort() }.getOrDefault(this)

/**
 * Performs a topological sort based on depth-first search as described
 * [here](https://en.wikipedia.org/wiki/Topological_sorting#Depth-first_search).
 *
 * Used to deal with some clients generating bad timestamps on thread chains.
 * @throws IllegalStateException thrown in case a cycle is detected making sort impossible to complete.
 */
fun List<FeedPost>.performTopologicalSort(): List<FeedPost> {
    val postsMap = this.associateBy { it.eventId }
    val adjacencyMap = mutableMapOf<String, MutableSet<String>>()

    val permanentMark = mutableSetOf<String>()
    val temporaryMark = mutableSetOf<String>()
    val finalList = mutableListOf<FeedPost>()

    this.forEach { post ->
        post.tags.find { it.hasReplyMarker() }?.getTagValueOrNull()?.let {
            adjacencyMap.getOrPut(key = it) { mutableSetOf() }
                .add(post.eventId)
        }
        post.tags.find { it.hasRootMarker() }?.getTagValueOrNull()?.let {
            adjacencyMap.getOrPut(key = it) { mutableSetOf() }
                .add(post.eventId)
        }
    }

    fun visit(node: FeedPost) {
        if (permanentMark.contains(node.eventId)) return
        if (temporaryMark.contains(node.eventId)) error("Impossible. Graph has cycle.")

        temporaryMark.add(node.eventId)

        adjacencyMap.getOrElse(key = node.eventId) { emptySet<String>() }
            .forEach { id ->
                postsMap[id]?.let { visit(it) }
            }

        permanentMark.add(node.eventId)
        finalList.add(0, node)
    }

    this.forEach { visit(it) }
    return finalList
}
