package net.primal.android.thread.articles

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import net.primal.android.thread.articles.ArticleContract.SideEffect
import net.primal.android.thread.articles.ArticleContract.UiEvent
import net.primal.core.testing.CoroutinesTestRule
import net.primal.domain.reads.Article
import net.primal.domain.reads.ArticleRepository
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ArticleViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val aTag = "30023:author:article-1"

    private val article = Article(
        aTag = aTag,
        eventId = "eventId",
        articleId = "article-1",
        authorId = "author",
        createdAt = 0L,
        content = "full article markdown",
        title = "Title",
        publishedAt = 0L,
        articleRawJson = "{\"kind\":30023}",
    )

    private fun buildViewModel(articleRepository: ArticleRepository) =
        ArticleViewModel(
            activeAccountStore = mockk(relaxed = true),
            articleRepository = articleRepository,
            profileRepository = mockk(relaxed = true),
            mutedItemRepository = mockk(relaxed = true),
            bookmarksRepository = mockk(relaxed = true),
            relayHintsRepository = mockk(relaxed = true),
            eventInteractionRepository = mockk(relaxed = true),
        )

    @Test
    fun copyArticleTextAction_emitsCopyTextEffectWithArticleContent() =
        runTest {
            val repository = mockk<ArticleRepository> {
                coEvery { getArticleByATag(aTag) } returns article
            }
            val viewModel = buildViewModel(articleRepository = repository)

            viewModel.setEvent(UiEvent.CopyArticleTextAction(articleATag = aTag))

            viewModel.effects.first() shouldBe SideEffect.CopyText(text = "full article markdown")
        }

    @Test
    fun copyRawDataAction_emitsCopyTextEffectWithRawJson() =
        runTest {
            val repository = mockk<ArticleRepository> {
                coEvery { getArticleByATag(aTag) } returns article
            }
            val viewModel = buildViewModel(articleRepository = repository)

            viewModel.setEvent(UiEvent.CopyRawDataAction(articleATag = aTag))

            viewModel.effects.first() shouldBe SideEffect.CopyText(text = "{\"kind\":30023}")
        }

    @Test
    fun copyArticleTextAction_emitsNothingWhenArticleMissing() =
        runTest {
            val repository = mockk<ArticleRepository> {
                coEvery { getArticleByATag(aTag) } returns null
            }
            val viewModel = buildViewModel(articleRepository = repository)

            viewModel.setEvent(UiEvent.CopyArticleTextAction(articleATag = aTag))

            withTimeoutOrNull(timeMillis = 100) { viewModel.effects.first() } shouldBe null
        }
}
