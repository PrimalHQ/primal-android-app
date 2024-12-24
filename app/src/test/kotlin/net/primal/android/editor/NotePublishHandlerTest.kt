package net.primal.android.editor

import android.net.Uri
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.lang.reflect.Method
import java.util.UUID
import kotlin.math.exp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.notes.db.PostData
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class NotePublishHandlerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @Test
    fun `buildRefinedContent returns correct with zero attachments`() {
        val notePublisher = buildNotePublishHandler()

        val buildRefinedContent = notePublisher.getPrivateMethod(
            "buildRefinedContent",
            List::class.java,
            String::class.java,
        )

        val expectedContent = "some content"

        val actualContent = buildRefinedContent.invoke(notePublisher, emptyList<NoteAttachment>(), expectedContent)

        actualContent shouldBe expectedContent
    }

    @Test
    fun `buildRefinedContent returns correct with single attachment`() {
        val notePublisher = buildNotePublishHandler()

        val buildRefinedContent = notePublisher.getPrivateMethod(
            "buildRefinedContent",
            List::class.java,
            String::class.java,
        )

        val attachment = buildNoteAttachment(remoteUrl = "https://primal.net")
        val content = "some content"

        val expectedContent = "$content\n\n${attachment.remoteUrl}\n"

        val actualContent = buildRefinedContent.invoke(notePublisher, listOf<NoteAttachment>(attachment), content)

        actualContent shouldBe expectedContent
    }

    @Test
    fun `buildRefinedContent returns correct with multiple attachments`() {
        val notePublisher = buildNotePublishHandler()

        val buildRefinedContent = notePublisher.getPrivateMethod(
            "buildRefinedContent",
            List::class.java,
            String::class.java,
        )

        val attachments = listOf(
            buildNoteAttachment(remoteUrl = "https://primal.net"),
            buildNoteAttachment(remoteUrl = "https://primal.net/somemedia"),
        )
        val content = "some content"

        val expectedContent = "$content\n\n${attachments[0].remoteUrl}\n${attachments[1].remoteUrl}\n"

        val actualContent = buildRefinedContent.invoke(notePublisher, attachments, content)

        actualContent shouldBe expectedContent
    }

    @Test
    fun `constructReplyTags returns null when replying post is same as root`() {
        val notePublisher = buildNotePublishHandler()

        val constructReplyTags = notePublisher.getPrivateMethod(
            "constructReplyTags",
            String::class.java,
            String::class.java,
        )

        constructReplyTags.invoke(notePublisher, "test", "test") shouldBe null
    }

    @Test
    fun `constructReplyTags calls asEventIdTag when replying post is different then root`() {
        val notePublisher = buildNotePublishHandler()

        val constructReplyTags = notePublisher.getPrivateMethod(
            "constructReplyTags",
            String::class.java,
            String::class.java,
        )
        mockkStatic(String::asEventIdTag)

        val replyToNoteId = "some note id"
        val rootNoteId = "some root note id"

        constructReplyTags(notePublisher, replyToNoteId, rootNoteId)

        verify(exactly = 1) {
            replyToNoteId.asEventIdTag(marker = "reply")
        }
    }

    @Test
    fun `constructPubkeyTags returns correct set when highlight author is missing`() {
        val notePublisher = buildNotePublishHandler()

        val constructPubkeyTags = notePublisher.getPrivateMethod(
            "constructPubkeyTags",
            PostData::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
        )

        val replyPostData = buildPostData()
        val replyToAuthorId = "some author id"
        val rootArticleAuthorId = "some article author id"

        val replyToAuthorPubkey = buildJsonArray { add(JsonPrimitive(replyToAuthorId)) }
        val rootArticleAuthorPubkey = buildJsonArray { add(JsonPrimitive(rootArticleAuthorId)) }

        val expectedSet = setOf(
            replyToAuthorPubkey,
            rootArticleAuthorPubkey,
        )

        mockkStatic(String::asPubkeyTag)
        every { replyToAuthorId.asPubkeyTag() } returns replyToAuthorPubkey
        every { rootArticleAuthorId.asPubkeyTag() } returns rootArticleAuthorPubkey

        val actualSet = constructPubkeyTags(
            notePublisher,
            replyPostData,
            replyToAuthorId,
            null,
            rootArticleAuthorId,
        )

        verify(exactly = 1) {
            replyToAuthorId.asPubkeyTag()
            rootArticleAuthorId.asPubkeyTag()
        }

        actualSet shouldBe expectedSet
    }


    @Test
    fun `constructPubkeyTags returns highlight author when both article and highlight present`() {
        val notePublisher = buildNotePublishHandler()

        val constructPubkeyTags = notePublisher.getPrivateMethod(
            "constructPubkeyTags",
            PostData::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
        )

        val replyPostData = buildPostData()
        val replyToAuthorId = "some author id"
        val rootHighlightAuthorId = "some highlight author id"
        val rootArticleAuthorId = "some article author id"

        val replyToAuthorPubkey = buildJsonArray { add(JsonPrimitive(replyToAuthorId)) }
        val rootHighlightAuthorPubkey = buildJsonArray { add(JsonPrimitive(rootHighlightAuthorId)) }
        val rootArticleAuthorPubkey = buildJsonArray { add(JsonPrimitive(rootArticleAuthorId)) }

        val expectedSet = setOf(
            replyToAuthorPubkey,
            rootHighlightAuthorPubkey,
        )

        mockkStatic(String::asPubkeyTag)
        every { replyToAuthorId.asPubkeyTag() } returns replyToAuthorPubkey
        every { rootHighlightAuthorId.asPubkeyTag() } returns rootHighlightAuthorPubkey
        every { rootArticleAuthorId.asPubkeyTag() } returns rootArticleAuthorPubkey

        val actualSet = constructPubkeyTags(
            notePublisher,
            replyPostData,
            replyToAuthorId,
            rootHighlightAuthorId,
            rootArticleAuthorId,
        )

        verify(exactly = 1) {
            replyToAuthorId.asPubkeyTag()
            rootHighlightAuthorId.asPubkeyTag()
            rootArticleAuthorId.asPubkeyTag()
        }

        actualSet shouldBe expectedSet
    }

    @Test
    fun `constructPubkeyTags keeps existing pubkey tags`() {
        val notePublisher = buildNotePublishHandler()

        val constructPubkeyTags = notePublisher.getPrivateMethod(
            "constructPubkeyTags",
            PostData::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
        )

        val existingTags = listOf("test".asPubkeyTag())
        val replyPostData = buildPostData(
            tags = existingTags
        )
        val replyToAuthorId = "some author id"
        val replyToAuthorPubkey = replyToAuthorId.asPubkeyTag()

        val expectedSet = setOf(
            existingTags,
            listOf(replyToAuthorPubkey),
        ).flatten()

        mockkStatic(JsonArray::isPubKeyTag)

        val actualSet = constructPubkeyTags(
            notePublisher,
            replyPostData,
            replyToAuthorId,
            null,
            null,
        )

        actualSet shouldBe expectedSet
    }

    private fun buildNotePublishHandler(
        dispatcherProvider: CoroutineDispatcherProvider = coroutinesTestRule.dispatcherProvider,
        nostrPublisher: NostrPublisher = mockk(),
        database: PrimalDatabase = mockk(),
    ) = NotePublishHandler(
        dispatcherProvider = dispatcherProvider,
        nostrPublisher = nostrPublisher,
        database = database,
    )

    private fun buildPostData(
        postId: String = UUID.randomUUID().toString(),
        authorId: String = UUID.randomUUID().toString(),
        createdAt: Long = Clock.System.now().epochSeconds,
        tags: List<JsonArray> = emptyList(),
        content: String = "",
        uris: List<String> = emptyList(),
        hashtags: List<String> = emptyList(),
        sig: String = "",
        raw: String = "",
        authorMetadataId: String? = null,
        replyToPostId: String? = null,
        replyToAuthorId: String? = null,
    ) = PostData(
        postId = postId,
        authorId = authorId,
        createdAt = createdAt,
        tags = tags,
        content = content,
        uris = uris,
        hashtags = hashtags,
        sig = sig,
        raw = raw,
        authorMetadataId = authorMetadataId,
        replyToPostId = replyToPostId,
        replyToAuthorId = replyToAuthorId,
    )

    private fun buildNoteAttachment(
        id: UUID = UUID.randomUUID(),
        localUri: Uri = mockk(),
        remoteUrl: String? = null,
        mimeType: String? = null,
        originalHash: String? = null,
        uploadedHash: String? = null,
        originalUploadedInBytes: Int? = null,
        originalSizeInBytes: Int? = null,
        uploadedSizeInBytes: Int? = null,
        dimensionInPixels: String? = null,
        uploadError: Throwable? = null,
    ) = NoteAttachment(
        id = id,
        localUri = localUri,
        remoteUrl = remoteUrl,
        mimeType = mimeType,
        originalHash = originalHash,
        originalUploadedInBytes = originalUploadedInBytes,
        uploadedHash = uploadedHash,
        originalSizeInBytes = originalSizeInBytes,
        uploadedSizeInBytes = uploadedSizeInBytes,
        dimensionInPixels = dimensionInPixels,
        uploadError = uploadError,
    )

    private fun NotePublishHandler.getPrivateMethod(name: String, vararg classParameters: Class<*>): Method {
        val method = this.javaClass.getDeclaredMethod(name, *classParameters)
        method.isAccessible = true

        return method
    }
}
