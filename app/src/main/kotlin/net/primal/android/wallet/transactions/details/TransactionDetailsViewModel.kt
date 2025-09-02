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
import net.primal.android.wallet.transactions.details.TransactionDetailsContract.UiState
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.getIfTypeOrNull
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.Nprofile
import net.primal.domain.nostr.asATagValue
import net.primal.domain.posts.FeedRepository
import net.primal.domain.reads.ArticleRepository
import net.primal.domain.streams.StreamRepository
import net.primal.domain.streams.mappers.asReferencedStream
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.WalletRepository
import timber.log.Timber

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val feedRepository: FeedRepository,
    private val articleRepository: ArticleRepository,
    private val streamRepository: StreamRepository,
    private val eventRepository: EventRepository,
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
            if (tx is Transaction.Zap) {
                tx.zappedEntity.let { entity ->
                    when (entity) {
                        is Naddr -> {
                            fetchAndObserveNaddrEntity(entity)
                        }

                        is Nevent -> {
                            observeZappedNote(entity.eventId)
                            fetchZappedNote(entity.eventId)
                        }

                        is Nprofile -> Unit
                    }
                }
            }
        }

    private fun fetchAndObserveNaddrEntity(naddr: Naddr) {
        when (naddr.kind) {
            NostrEventKind.LongFormContent.value -> {
                observeZappedArticle(naddr = naddr)
                fetchZappedArticle(articleId = naddr.identifier, articleAuthorId = naddr.userId)
            }

            NostrEventKind.LiveActivity.value -> {
                observeZappedStream(naddr = naddr)
                fetchZappedStream(naddr = naddr)
            }

            else -> Unit
        }
    }

    private fun observeZappedStream(naddr: Naddr) =
        viewModelScope.launch {
            streamRepository.observeStream(aTag = naddr.asATagValue())
                .collect {
                    setState { copy(referencedStream = it?.asReferencedStream()) }
                }
        }

    private fun fetchZappedStream(naddr: Naddr) =
        viewModelScope.launch {
            eventRepository.fetchReplaceableEvent(naddr = naddr)
        }

    private fun observeZappedArticle(naddr: Naddr) =
        viewModelScope.launch {
            articleRepository.observeArticle(aTag = naddr.asATagValue())
                .collect {
                    setState { copy(articlePost = it.mapAsFeedArticleUi()) }
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

    private fun Transaction.mapAsTransactionDataUi() =
        TransactionDetailDataUi(
            txId = this.transactionId,
            txType = this.type,
            txState = this.state,
            txAmountInSats = this.amountInBtc.toBigDecimal().abs().toSats(),
            txAmountInUsd = this.amountInUsd,
            txInstant = Instant.ofEpochSecond(this.completedAt ?: this.createdAt),
            txNote = this.note,
            invoice = this.invoice,
            totalFeeInSats = this.totalFeeInBtc?.toBigDecimal()?.abs()?.toSats(),
            exchangeRate = this.exchangeRate,
            onChainAddress = this.getIfTypeOrNull(Transaction.OnChain::onChainAddress),
            onChainTxId = this.getIfTypeOrNull(Transaction.OnChain::onChainTxId),
            otherUserId = this.getIfTypeOrNull(Transaction.Zap::otherUserId),
            otherUserAvatarCdnImage = this.getIfTypeOrNull(Transaction.Zap::otherUserProfile)?.avatarCdnImage
                ?: this.getIfTypeOrNull(Transaction.Lightning::otherUserProfile)?.avatarCdnImage,
            otherUserDisplayName = this.getIfTypeOrNull(Transaction.Zap::otherUserProfile)
                ?.authorNameUiFriendly()
                ?: this.getIfTypeOrNull(Transaction.Lightning::otherUserProfile)?.authorNameUiFriendly(),
            otherUserInternetIdentifier = this.getIfTypeOrNull(Transaction.Zap::otherUserProfile)
                ?.internetIdentifier
                ?: this.getIfTypeOrNull(Transaction.Lightning::otherUserProfile)?.internetIdentifier,
            otherUserLegendaryCustomization = this.getIfTypeOrNull(Transaction.Zap::otherUserProfile)
                ?.primalPremiumInfo?.legendProfile?.asLegendaryCustomization()
                ?: this.getIfTypeOrNull(Transaction.Lightning::otherUserProfile)
                    ?.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
            otherUserLightningAddress = this.getIfTypeOrNull(Transaction.Zap::otherLightningAddress)
                ?: this.getIfTypeOrNull(Transaction.Lightning::otherLightningAddress),
            isZap = this is Transaction.Zap,
            isStorePurchase = this is Transaction.StorePurchase,
        )
}
