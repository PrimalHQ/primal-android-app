package net.primal.android.wallet.di

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import net.primal.domain.wallet.sync.WalletDataSyncer

fun ViewModel.bindToProcessLifecycle(syncer: WalletDataSyncer) {
    val lifecycle = ProcessLifecycleOwner.get().lifecycle
    val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> syncer.start()
            Lifecycle.Event.ON_PAUSE -> syncer.stop()
            else -> Unit
        }
    }
    syncer.start()
    lifecycle.addObserver(observer)
    addCloseable {
        lifecycle.removeObserver(observer)
        syncer.stop()
    }
}
