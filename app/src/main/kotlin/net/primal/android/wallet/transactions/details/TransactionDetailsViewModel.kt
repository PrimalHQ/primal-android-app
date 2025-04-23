package net.primal.android.wallet.transactions.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
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
import net.primal.android.articles.feed.ui.mapAsFeedArticleUi
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.navigation.transactionIdOrThrow
import net.primal.android.notes.feed.model.asFeedPostUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.TransactionProfileData
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.details.TransactionDetailsContract.UiState
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.posts.FeedRepository
import net.primal.domain.reads.ArticleRepository
import timber.log.Timber

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
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
            val tx = walletRepository.findTransactionByIdOrNull(txId = transactionId)
            setState { copy(loading = false, txData = tx?.mapAsTransactionDataUi()) }
            tx?.transaction?.zapNoteId?.let {
                observeZappedNote(it)
                fetchZappedNote(it)
                observeZappedArticle(articleId = it, articleAuthorId = tx.transaction.zapNoteAuthorId)
                fetchZappedArticle(articleId = it, articleAuthorId = tx.transaction.zapNoteAuthorId)
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
            } catch (error: NetworkException) {
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
            } catch (error: NetworkException) {
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

    private fun TransactionProfileData.mapAsTransactionDataUi() =
        TransactionDetailDataUi(
            txId = this.transaction.id,
            txType = this.transaction.type,
            txState = this.transaction.state,
            txAmountInSats = this.transaction.amountInBtc.toBigDecimal().abs().toSats(),
            txAmountInUsd = this.transaction.amountInUsd,
            txInstant = Instant.ofEpochSecond(this.transaction.completedAt ?: this.transaction.createdAt),
            txNote = this.transaction.note,
            invoice = this.transaction.invoice,
            totalFeeInSats = this.transaction.totalFeeInBtc?.toBigDecimal()?.abs()?.toSats(),
            exchangeRate = this.transaction.exchangeRate,
            onChainAddress = this.transaction.onChainAddress,
            onChainTxId = this.transaction.onChainTxId,
            otherUserId = this.transaction.otherUserId,
            otherUserAvatarCdnImage = this.otherProfileData?.avatarCdnImage,
            otherUserDisplayName = this.otherProfileData?.authorNameUiFriendly(),
            otherUserInternetIdentifier = this.otherProfileData?.internetIdentifier,
            otherUserLegendaryCustomization = this.otherProfileData?.primalPremiumInfo
                ?.legendProfile?.asLegendaryCustomization(),
            otherUserLightningAddress = this.transaction.otherLightningAddress,
            isZap = this.transaction.isZap,
            isStorePurchase = this.transaction.isStorePurchase,
        )
}
