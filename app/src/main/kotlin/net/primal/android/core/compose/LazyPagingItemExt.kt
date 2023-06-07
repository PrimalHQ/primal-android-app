package net.primal.android.core.compose

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

fun <T : Any> LazyPagingItems<T>.isEmpty(): Boolean = itemCount <= 0

fun <T : Any> LazyPagingItems<T>.isMediatorPrependLoading(): Boolean = loadState.mediator?.prepend == LoadState.Loading

fun <T : Any> LazyPagingItems<T>.isMediatorAppendLoading(): Boolean = loadState.mediator?.append == LoadState.Loading
