package net.primal.android.thread.articles.details

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.navigation.ARTICLE_NADDR
import net.primal.android.thread.articles.details.ArticleDetailsContract.ArticlePartRender
import net.primal.core.testing.CoroutinesTestRule
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.cryptography.utils.hexToNoteHrp
import net.primal.domain.posts.FeedPost
import net.primal.domain.posts.FeedPostAuthor
import net.primal.domain.posts.FeedRepository
import net.primal.domain.reads.Article
import net.primal.domain.reads.ArticleRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ArticleDetailsViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val userId = "aa4fc8665f5696e33db7e1a572e3b0f5b3d615837b0f362dcb1c8068b098c7b4"
    private val noteId = "5b3d615837b0f362dcb1c8068b098c7b4aa4fc8665f5696e33db7e1a572e3b0f"
    private val naddr = Naddr(
        identifier = "test-article",
        userId = userId,
        kind = NostrEventKind.LongFormContent.value,
    )

    private fun article(content: String, uris: List<String> = emptyList()) =
        Article(
            aTag = naddr.asATagValue(),
            eventId = "eventId",
            articleId = naddr.identifier,
            authorId = userId,
            createdAt = 0L,
            content = content,
            title = "Title",
            publishedAt = 0L,
            articleRawJson = "{}",
            uris = uris,
        )

    private fun feedPost(eventId: String) =
        FeedPost(
            eventId = eventId,
            author = FeedPostAuthor(authorId = userId, handle = "alice", displayName = "Alice"),
            kind = 1,
            content = "referenced note",
            tags = emptyList(),
            timestamp = Instant.fromEpochSeconds(0),
            rawNostrEvent = "{}",
        )

    private fun articleRepository(articleFlow: MutableSharedFlow<Article>): ArticleRepository =
        mockk(relaxed = true) {
            coEvery { observeArticle(any(), any()) } returns articleFlow
            coEvery { observeArticleComments(any(), any(), any()) } returns emptyFlow()
        }

    private fun createViewModel(
        articleRepository: ArticleRepository,
        feedRepository: FeedRepository,
    ): ArticleDetailsViewModel {
        return ArticleDetailsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(ARTICLE_NADDR to naddr.toNaddrString())),
            activeAccountStore = mockk(relaxed = true) {
                every { activeUserId() } returns userId
                every { activeAccountState } returns emptyFlow()
            },
            articleRepository = articleRepository,
            feedRepository = feedRepository,
            highlightRepository = mockk(relaxed = true),
            profileRepository = mockk(relaxed = true) {
                coEvery { findProfileData(any()) } returns emptyList()
            },
            profileFollowsHandler = mockk(relaxed = true) {
                every { observeResults() } returns emptyFlow()
            },
            eventInteractionRepository = mockk(relaxed = true),
            zapHandler = mockk(relaxed = true),
            walletAccountRepository = mockk(relaxed = true),
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
        )
    }

    @Test
    fun `article content is segmented into articleParts`() =
        runTest {
            val articleFlow = MutableSharedFlow<Article>(replay = 1)
            val feedRepository = mockk<FeedRepository>(relaxed = true) {
                coEvery { findAllPostsByIds(any()) } returns emptyList()
            }
            val viewModel = createViewModel(articleRepository(articleFlow), feedRepository)

            articleFlow.emit(article(content = "Hello **world**"))
            advanceUntilIdle()

            viewModel.state.value.articleParts shouldBe listOf(
                ArticlePartRender.MarkdownRender(markdown = "Hello **world**"),
            )
        }

    @Test
    fun `referenced note arriving after first emission produces NoteRender`() =
        runTest {
            val noteUri = "nostr:${noteId.hexToNoteHrp()}"
            val articleFlow = MutableSharedFlow<Article>(replay = 1)
            var cachedNotes = emptyList<FeedPost>()
            val feedRepository = mockk<FeedRepository>(relaxed = true) {
                coEvery { findAllPostsByIds(any()) } coAnswers { cachedNotes }
            }
            val viewModel = createViewModel(articleRepository(articleFlow), feedRepository)

            articleFlow.emit(article(content = "intro\n\n$noteUri", uris = listOf(noteUri)))
            advanceUntilIdle()

            viewModel.state.value.articleParts.last()
                .shouldBeInstanceOf<ArticlePartRender.MarkdownRender>()

            cachedNotes = listOf(feedPost(eventId = noteId))
            articleFlow.emit(article(content = "intro\n\n$noteUri", uris = listOf(noteUri)))
            advanceUntilIdle()

            viewModel.state.value.articleParts.last()
                .shouldBeInstanceOf<ArticlePartRender.NoteRender>()
        }
}
