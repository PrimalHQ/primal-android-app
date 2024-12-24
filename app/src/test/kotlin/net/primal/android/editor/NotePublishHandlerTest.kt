package net.primal.android.editor

import android.net.Uri
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.lang.reflect.Method
import java.util.UUID
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
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.model.NostrEventKind
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

        val expectedContent = "some content"

        val actualContent = notePublisher.getBuildRefinedContent()
            .invoke(notePublisher, emptyList<NoteAttachment>(), expectedContent)

        actualContent shouldBe expectedContent
    }

    @Test
    fun `buildRefinedContent returns correct with single attachment`() {
        val notePublisher = buildNotePublishHandler()

        val attachment = buildNoteAttachment(remoteUrl = "https://primal.net")
        val content = "some content"

        val expectedContent = "$content\n\n${attachment.remoteUrl}\n"

        val actualContent = notePublisher.getBuildRefinedContent()
            .invoke(notePublisher, listOf<NoteAttachment>(attachment), content)

        actualContent shouldBe expectedContent
    }

    @Test
    fun `buildRefinedContent returns correct with multiple attachments`() {
        val notePublisher = buildNotePublishHandler()

        val attachments = listOf(
            buildNoteAttachment(remoteUrl = "https://primal.net"),
            buildNoteAttachment(remoteUrl = "https://primal.net/somemedia"),
        )
        val content = "some content"

        val expectedContent = "$content\n\n${attachments[0].remoteUrl}\n${attachments[1].remoteUrl}\n"

        val actualContent = notePublisher.getBuildRefinedContent()
            .invoke(notePublisher, attachments, content)

        actualContent shouldBe expectedContent
    }

    @Test
    fun `constructReplyTags returns null when replying post is same as root`() {
        val notePublisher = buildNotePublishHandler()

        notePublisher.getConstructReplyTags()
            .invoke(notePublisher, "test", "test") shouldBe null
    }

    @Test
    fun `constructReplyTags calls asEventIdTag when replying post is different then root`() {
        val notePublisher = buildNotePublishHandler()

        mockkStatic(String::asEventIdTag)

        val replyToNoteId = "some note id"
        val rootNoteId = "some root note id"

        notePublisher.getConstructReplyTags().invoke(notePublisher, replyToNoteId, rootNoteId)

        verify(exactly = 1) {
            replyToNoteId.asEventIdTag(marker = "reply")
        }
    }

    @Test
    fun `constructPubkeyTags returns correct set when highlight author is missing`() {
        val notePublisher = buildNotePublishHandler()

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

        val actualSet = notePublisher.getConstructPubkeyTags()
            .invoke(
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

        val actualSet = notePublisher.getConstructPubkeyTags()
            .invoke(
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

        val existingTags = listOf("test".asPubkeyTag())
        val replyPostData = buildPostData(
            tags = existingTags,
        )
        val replyToAuthorId = "some author id"
        val replyToAuthorPubkey = replyToAuthorId.asPubkeyTag()

        val expectedSet = setOf(
            existingTags,
            listOf(replyToAuthorPubkey),
        ).flatten()

        mockkStatic(JsonArray::isPubKeyTag)

        val actualSet = notePublisher.getConstructPubkeyTags()
            .invoke(
                notePublisher,
                replyPostData,
                replyToAuthorId,
                null,
                null,
            )

        actualSet shouldBe expectedSet
    }

    @Test
    fun `constructRootTags returns empty list if no root events provided`() {
        val notePublisher = buildNotePublishHandler()

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(notePublisher, null, null, null, null, null)

        actualTags shouldBe emptyList<JsonArray>()
    }

    @Test
    fun `constructRootTags returns highlight as root if only highlight available`() {
        val notePublisher = buildNotePublishHandler()

        val rootHighlightId = "someId"
        val expectedTags = listOf(rootHighlightId.asEventIdTag(marker = "root"))

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(notePublisher, rootHighlightId, null, null, null, null)

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns article as root if only article available`() {
        val notePublisher = buildNotePublishHandler()

        val rootArticleId = "someId"
        val rootArticleEventId = "someEventId"
        val rootArticleAuthorId = "someAuthorId"

        val expectedTags = listOf(
            rootArticleEventId.asEventIdTag(marker = "root"),
            "${NostrEventKind.LongFormContent.value}:$rootArticleAuthorId:$rootArticleId"
                .asReplaceableEventTag(marker = "root"),
        )

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(
                notePublisher,
                null,
                rootArticleId,
                rootArticleEventId,
                rootArticleAuthorId,
                null,
            )

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns post as root if only post available`() {
        val notePublisher = buildNotePublishHandler()

        val rootPostId = "someId"

        val expectedTags = listOf(rootPostId.asEventIdTag(marker = "root"))

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(notePublisher, null, null, null, null, rootPostId)

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns highlight as root if highlight and article available`() {
        val notePublisher = buildNotePublishHandler()

        val rootHighlightId = "someHighlightId"
        val rootArticleId = "someId"
        val rootArticleEventId = "someEventId"
        val rootArticleAuthorId = "someAuthorId"

        val expectedTags = listOf(rootHighlightId.asEventIdTag(marker = "root"))

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(
                notePublisher,
                rootHighlightId,
                rootArticleId,
                rootArticleEventId,
                rootArticleAuthorId,
                null,
            )

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns highlight as root if highlight and post available`() {
        val notePublisher = buildNotePublishHandler()

        val rootHighlightId = "someHighlightId"
        val rootPostId = "someId"

        val expectedTags = listOf(rootHighlightId.asEventIdTag(marker = "root"))

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(
                notePublisher,
                rootHighlightId,
                null,
                null,
                null,
                rootPostId,
            )

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns article as root if article and post available`() {
        val notePublisher = buildNotePublishHandler()

        val rootPostId = "somePostId"
        val rootArticleId = "someArticleId"
        val rootArticleEventId = "someEventId"
        val rootArticleAuthorId = "someAuthorId"

        val expectedTags = listOf(
            rootArticleEventId.asEventIdTag(marker = "root"),
            "${NostrEventKind.LongFormContent.value}:$rootArticleAuthorId:$rootArticleId"
                .asReplaceableEventTag(marker = "root"),
        )

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(
                notePublisher,
                null,
                rootArticleId,
                rootArticleEventId,
                rootArticleAuthorId,
                rootPostId,
            )

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns highlight as root if all available`() {
        val notePublisher = buildNotePublishHandler()

        val rootHighlightId = "someHighlightId"
        val rootPostId = "somePostId"
        val rootArticleId = "someArticleId"
        val rootArticleEventId = "someEventId"
        val rootArticleAuthorId = "someAuthorId"

        val expectedTags = listOf(
            rootHighlightId.asEventIdTag(marker = "root")
        )

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(
                notePublisher,
                rootHighlightId,
                rootArticleId,
                rootArticleEventId,
                rootArticleAuthorId,
                rootPostId,
            )

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns post as root if article is missing author data`() {
        val notePublisher = buildNotePublishHandler()

        val rootPostId = "somePostId"
        val rootArticleId = "someArticleId"
        val rootArticleEventId = "someEventId"

        val expectedTags = listOf(
            rootPostId.asEventIdTag(marker = "root")
        )

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(
                notePublisher,
                null,
                rootArticleId,
                rootArticleEventId,
                null,
                rootPostId,
            )

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns post as root if article is missing event data`() {
        val notePublisher = buildNotePublishHandler()

        val rootPostId = "somePostId"
        val rootArticleId = "someArticleId"
        val rootArticleAuthorId = "someAuthorId"

        val expectedTags = listOf(
            rootPostId.asEventIdTag(marker = "root")
        )

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(
                notePublisher,
                null,
                rootArticleId,
                null,
                rootArticleAuthorId,
                rootPostId,
            )

        actualTags shouldBe expectedTags
    }

    @Test
    fun `constructRootTags returns empty list if article is only available and is missing fields`() {
        val notePublisher = buildNotePublishHandler()

        val rootArticleId = "someArticleId"
        val rootArticleAuthorId = "someAuthorId"

        val actualTags = notePublisher.getConstructRootTags()
            .invoke(
                notePublisher,
                null,
                rootArticleId,
                null,
                rootArticleAuthorId,
                null,
            )

        actualTags shouldBe emptyList<JsonArray>()
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

    private fun NotePublishHandler.getConstructRootTags() =
        this.getPrivateMethod(
            "constructRootTags",
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
        )

    private fun NotePublishHandler.getConstructPubkeyTags() =
        this.getPrivateMethod(
            "constructPubkeyTags",
            PostData::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
        )

    private fun NotePublishHandler.getConstructReplyTags() =
        this.getPrivateMethod(
            "constructReplyTags",
            String::class.java,
            String::class.java,
        )

    private fun NotePublishHandler.getBuildRefinedContent() =
        this.getPrivateMethod(
            "buildRefinedContent",
            List::class.java,
            String::class.java,
        )

    private fun NotePublishHandler.getPrivateMethod(name: String, vararg parameterTypes: Class<*>): Method {
        val method = this.javaClass.getDeclaredMethod(name, *parameterTypes)
        method.isAccessible = true

        return method
    }
}
