package net.primal.android

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class Npub(val name: String)

data class Post(val name: String, val npubs : List<Npub>?)

fun posts(): Flow<List<Post>> {
    return flow {
        emit(listOf(Post("post1", null), Post("post2", null)))
    }
}

fun npubs(post: String): Flow<Npub> {
    return flow {
        emit(Npub("${post}_npub1"))
        emit(Npub("${post}_npub2"))
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
fun main() {
    runBlocking {
        posts()
            .flatMapConcat {
                it.map { post ->
                    npubs(post.name)
                        .flatMapMerge {  }
                }.merge()
                flowOf(post)
            }
            .collect {
                println(it)
            }
//        }
    }
}
