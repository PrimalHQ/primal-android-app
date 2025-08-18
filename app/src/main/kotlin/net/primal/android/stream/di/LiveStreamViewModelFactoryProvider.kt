package net.primal.android.stream.di

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import net.primal.android.stream.LiveStreamViewModel
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LiveStreamViewModelFactoryProvider {
    fun liveStreamViewModelFactory(): LiveStreamViewModel.Factory
}

@Composable
fun rememberLiveStreamViewModel(naddrUri: String?): LiveStreamViewModel? {
    val naddr: Naddr? = remember(naddrUri) {
        naddrUri?.let { Nip19TLV.parseUriAsNaddrOrNull(it) }
    }
    return rememberLiveStreamViewModel(naddr)
}

@Composable
fun rememberLiveStreamViewModel(naddr: Naddr?): LiveStreamViewModel? {
    if (naddr == null) return null

    val viewModelOwner: ViewModelStoreOwner = remember(naddr) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(viewModelOwner) {
        onDispose { viewModelOwner.viewModelStore.clear() }
    }

    val app = LocalContext.current.applicationContext as Application
    val assistedFactory = remember(naddr) {
        EntryPointAccessors.fromApplication(
            context = app,
            entryPoint = LiveStreamViewModelFactoryProvider::class.java,
        ).liveStreamViewModelFactory()
    }

    val factory = remember(naddr, assistedFactory) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(naddr) as T
            }
        }
    }

    return viewModel(
        viewModelStoreOwner = viewModelOwner,
        key = "live-stream:${naddr.userId}:${naddr.identifier}",
        factory = factory,
    )
}
