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
import net.primal.android.navigation.transactionIdOrThrow
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.db.WalletTransactionData
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.details.TransactionDetailsContract.UiState
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.repository.FeedRepository
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
            tx?.zapNoteId?.let {
                observeZappedNote(it)
                fetchZappedNote(it)
                observeZappedArticle(articleId = it, articleAuthorId = tx.zapNoteAuthorId)
                fetchZappedArticle(articleId = it, articleAuthorId = tx.zapNoteAuthorId)
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
                    articleRepository.fetchArticleAndComments(
                        userId = activeAccountStore.activeUserId(),
                        articleId = articleId,
                        articleAuthorId = articleAuthorId,
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeZappedNote(noteId: String) =
        viewModelScope.launch {
            feedRepository.observeConversation(userId = activeAccountStore.activeUserId(), noteId = noteId)
                .filter { it.isNotEmpty() }
                .mapNotNull { conversation -> conversation.first { it.eventId == noteId } }
                .collect {
                    setState { copy(feedPost = it.asFeedPostUi()) }
                }
        }

    private fun fetchZappedNote(noteId: String) =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                withContext(dispatcherProvider.io()) {
                    feedRepository.fetchReplies(userId = activeAccountStore.activeUserId(), noteId = noteId)
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

    private fun WalletTransactionData.mapAsTransactionDataUi() =
        TransactionDetailDataUi(
            txId = this.id,
            txType = this.type,
            txState = this.state,
            txAmountInSats = this.amountInBtc.toBigDecimal().abs().toSats(),
            txAmountInUsd = this.amountInUsd,
            txInstant = Instant.ofEpochSecond(this.completedAt ?: this.createdAt),
            txNote = this.note,
            invoice = this.invoice,
            totalFeeInSats = this.totalFeeInBtc?.toBigDecimal()?.abs()?.toSats(),
            exchangeRate = this.exchangeRate,
            onChainAddress = this.onChainAddress,
            onChainTxId = this.onChainTxId,
            otherUserId = this.otherUserId,
            // TODO We need to do TXs and ProfileData merging
//            otherUserAvatarCdnImage = this.otherProfileData?.avatarCdnImage,
//            otherUserDisplayName = this.otherProfileData?.authorNameUiFriendly(),
//            otherUserInternetIdentifier = this.otherProfileData?.internetIdentifier,
//            otherUserLegendaryCustomization = this.otherProfileData?.primalPremiumInfo
//                ?.legendProfile?.asLegendaryCustomization(),
            otherUserLightningAddress = this.otherLightningAddress,
            isZap = this.isZap,
            isStorePurchase = this.isStorePurchase,
        )
}
