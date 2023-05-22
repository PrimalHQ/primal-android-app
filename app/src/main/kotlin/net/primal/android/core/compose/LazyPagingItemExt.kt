package net.primal.android.core.compose

import androidx.paging.compose.LazyPagingItems

fun <T : Any> LazyPagingItems<T>.isEmpty(): Boolean = itemCount <= 0

