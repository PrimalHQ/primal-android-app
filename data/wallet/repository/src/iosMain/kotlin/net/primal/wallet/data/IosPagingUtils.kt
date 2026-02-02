package net.primal.wallet.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.IosPagingFactory
import net.primal.IosPagingPresenter
import net.primal.IosPagingSnapshot
import net.primal.domain.transactions.Transaction

object IosPagingUtils {

    // Option D: Typed for Transaction — no generics in Swift signature
    fun createTransactionPresenter(pagingFlow: Flow<PagingData<Transaction>>): IosPagingPresenter<Transaction> {
        return IosPagingFactory.createPresenter(pagingFlow)
    }

    fun createTransactionSnapshot(pagingFlow: Flow<PagingData<Transaction>>): IosPagingSnapshot<Transaction> {
        return IosPagingFactory.createSnapshot(pagingFlow)
    }
}
