package net.primal.android.wallet.transactions.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.articles.ArticleRepository
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.navigation.transactionIdOrThrow
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.notes.repository.FeedRepository
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.db.WalletTransaction
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.details.TransactionDetailsContract.UiState
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats
import timber.log.Timber

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val feedRepository: FeedRepository,
    private val articleRepository: ArticleRepository,
    private val exchangeRateHandler: ExchangeRateHandler,
) : ViewModel() {

    private val transactionId = savedStateHandle.transactionIdOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) =
        viewModelScope.launch { _state.getAndUpdate { it.reducer() } }

    init {
        loadTransaction()
        observeUsdExchangeRate()
    }

    private fun loadTransaction() =
        viewModelScope.launch {
            val tx = withContext(dispatcherProvider.io()) {
                walletRepository.findTransactionById(txId = transactionId)
            }
            setState { copy(loading = false, txData = tx?.mapAsTransactionDataUi()) }
            tx?.data?.zapNoteId?.let {
                observeZappedNote(it)
                fetchZappedNote(it)
                observeZappedArticle(articleId = it, articleAuthorId = tx.data.zapNoteAuthorId)
                fetchZappedArticle(articleId = it, articleAuthorId = tx.data.zapNoteAuthorId)
            }
        }

    private fun observeZappedArticle(articleId: String, articleAuthorId: String?) =
        viewModelScope.launch {
            articleAuthorId?.let {
                articleRepository.observeArticleByEventId(eventId = articleId, articleAuthorId = articleAuthorId)
                    .collect {
                        setState { copy(articlePost = it.mapAsFeedArticleUi()) }
                    }
            }
        }

    private fun fetchZappedArticle(articleId: String, articleAuthorId: String?) =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                articleAuthorId?.let {
                    articleRepository.fetchArticleAndComments(articleId = articleId, articleAuthorId = articleAuthorId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeZappedNote(noteId: String) =
        viewModelScope.launch {
            feedRepository.observeConversation(noteId = noteId)
                .filter { it.isNotEmpty() }
                .mapNotNull { conversation -> conversation.first { it.data.postId == noteId } }
                .collect {
                    setState { copy(feedPost = it.asFeedPostUi()) }
                }
        }

    private fun fetchZappedNote(noteId: String) =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                withContext(dispatcherProvider.io()) {
                    feedRepository.fetchReplies(noteId = noteId)
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeUsdExchangeRate() {
        viewModelScope.launch {
            fetchExchangeRate()
            exchangeRateHandler.usdExchangeRate.collect {
                setState { copy(currentExchangeRate = it) }
            }
        }
    }

    private fun fetchExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.updateExchangeRate(
                userId = activeAccountStore.activeUserId(),
            )
        }

    private fun WalletTransaction.mapAsTransactionDataUi() =
        TransactionDetailDataUi(
            txId = this.data.id,
            txType = this.data.type,
            txState = this.data.state,
            txAmountInSats = this.data.amountInBtc.toBigDecimal().abs().toSats(),
            txAmountInUsd = this.data.amountInUsd,
            txInstant = Instant.ofEpochSecond(this.data.completedAt ?: this.data.createdAt),
            txNote = this.data.note,
            invoice = this.data.invoice,
            totalFeeInSats = this.data.totalFeeInBtc?.toBigDecimal()?.abs()?.toSats(),
            exchangeRate = this.data.exchangeRate,
            onChainAddress = this.data.onChainAddress,
            onChainTxId = this.data.onChainTxId,
            otherUserId = this.data.otherUserId,
            otherUserAvatarCdnImage = this.otherProfileData?.avatarCdnImage,
            otherUserDisplayName = this.otherProfileData?.authorNameUiFriendly(),
            otherUserInternetIdentifier = this.otherProfileData?.internetIdentifier,
            otherUserLightningAddress = this.data.otherLightningAddress,
            otherUserLegendaryCustomization = this.otherProfileData?.primalPremiumInfo
                ?.legendProfile?.asLegendaryCustomization(),
            isZap = this.data.isZap,
            isStorePurchase = this.data.isStorePurchase,
        )
}
