package net.primal.android.editor

import android.net.Uri
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.nostr.db.EventRelayHints
import net.primal.android.nostr.db.EventRelayHintsDao
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asEventTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.isATag
import net.primal.android.nostr.ext.isEventIdTag
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nevent
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import net.primal.android.nostr.utils.Nip19TLV.toNeventString
import net.primal.android.nostr.utils.asATagValue
import net.primal.android.notes.db.PostDao
import net.primal.android.notes.db.PostData
import org.junit.Rule
import org.junit.Test

@Suppress("LargeClass")
@OptIn(ExperimentalCoroutinesApi::class)
class NotePublishHandlerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val expectedUserId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079"

    private fun mockkPrimalDatabase(
        post: PostData? = null,
        eventHints: List<EventRelayHints> = emptyList(),
    ): PrimalDatabase {
        val postDao = mockk<PostDao> {
            every { findByPostId(any()) } returns post
        }
        val eventHintsDao = mockk<EventRelayHintsDao> {
            coEvery { findById(any()) } returns eventHints
        }

        return mockk<PrimalDatabase> {
            every { posts() } returns postDao
            every { eventHints() } returns eventHintsDao
        }
    }

    private fun buildHighlightNevent() =
        Nevent(
            kind = NostrEventKind.Highlight.value,
            userId = "highlightAuthorId",
            eventId = "highlightEventId",
        )

    private fun buildArticleNaddr() =
        Naddr(
            kind = NostrEventKind.LongFormContent.value,
            userId = "articleAuthorId",
            identifier = "articleIdentifier",
        )

    private fun buildNoteNevent(eventId: String = "noteEventId") =
        Nevent(
            kind = NostrEventKind.ShortTextNote.value,
            userId = "noteAuthorID",
            eventId = eventId,
        )

    private fun buildNotePublishHandler(
        dispatcherProvider: CoroutineDispatcherProvider = coroutinesTestRule.dispatcherProvider,
        nostrPublisher: NostrPublisher = mockk(),
        database: PrimalDatabase = mockkPrimalDatabase(),
    ) = NotePublishHandler(
        dispatcherProvider = dispatcherProvider,
        nostrPublisher = nostrPublisher,
        database = database,
    )

//    private fun buildPostData(
//        postId: String = UUID.randomUUID().toString(),
//        authorId: String = UUID.randomUUID().toString(),
//        createdAt: Long = Clock.System.now().epochSeconds,
//        tags: List<JsonArray> = emptyList(),
//        content: String = "",
//        uris: List<String> = emptyList(),
//        hashtags: List<String> = emptyList(),
//        sig: String = "",
//        raw: String = "",
//        authorMetadataId: String? = null,
//        replyToPostId: String? = null,
//        replyToAuthorId: String? = null,
//    ) = PostData(
//        postId = postId,
//        authorId = authorId,
//        createdAt = createdAt,
//        tags = tags,
//        content = content,
//        uris = uris,
//        hashtags = hashtags,
//        sig = sig,
//        raw = raw,
//        authorMetadataId = authorMetadataId,
//        replyToPostId = replyToPostId,
//        replyToAuthorId = replyToAuthorId,
//    )

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

    /**
     * User Ids
     */

    @Test
    fun publishShortTextNote_callsNostrPublisher_withGivenUserId() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(userId = expectedUserId, content = "")

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    any(),
                )
            }
        }

    @Test
    fun publishShortTextNote_authorsNostrEvent_withGivenUserId() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(userId = expectedUserId, content = "")

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { it.pubKey shouldBe expectedUserId },
                )
            }
        }

    /**
     * Content & Attachments
     */

    @Test
    fun publishShortTextNote_publishesOriginalContent_ifNoAttachmentsGiven() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val givenContent = "This is note without attachments."
            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = givenContent,
                attachments = emptyList(),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg {
                        it.content shouldBe givenContent
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_appendsAttachmentUrl_toGivenContent() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val givenContent = "This is note with attachments."
            val expectedRemoteUrl = "https://m.primal.net/abc"
            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = givenContent,
                attachments = listOf(buildNoteAttachment(remoteUrl = expectedRemoteUrl)),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg {
                        it.content shouldStartWith givenContent
                        it.content shouldEndWith expectedRemoteUrl
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_appendsAllAttachmentUrls_toGivenContent() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val givenContent = "This is note with attachments."
            val expectedRemoteUrl1 = "https://m.primal.net/abc"
            val expectedAttachment1 = buildNoteAttachment(remoteUrl = expectedRemoteUrl1)
            val expectedRemoteUrl2 = "https://m.primal.net/def"
            val expectedAttachment2 = buildNoteAttachment(remoteUrl = expectedRemoteUrl2)
            val expectedUrlsAppendix = "$expectedRemoteUrl1\n$expectedRemoteUrl2"

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = givenContent,
                attachments = listOf(expectedAttachment1, expectedAttachment2),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg {
                        it.content shouldStartWith givenContent
                        it.content shouldEndWith expectedUrlsAppendix
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_addsExtraLine_betweenContentAndAttachments() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val givenContent = "This is note with attachments."
            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = givenContent,
                attachments = listOf(buildNoteAttachment(remoteUrl = "remoteUrl")),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg {
                        val appendix = it.content.substringAfter(givenContent)
                        val breakLines = appendix.substring(startIndex = 0, endIndex = 3)
                        breakLines[0] shouldBe '\n'
                        breakLines[1] shouldBe '\n'
                        breakLines[2] shouldNotBe '\n'
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_createsIMetaTag_forGivenImageAttachments() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val expectedAttachments = listOf(
                buildNoteAttachment(mimeType = "image/jpeg", remoteUrl = "https://m.primal.net/abc"),
                buildNoteAttachment(mimeType = "image/jpeg", remoteUrl = "https://m.primal.net/def"),
            )

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "",
                attachments = expectedAttachments,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg {
                        val iMetaTagCount = it.tags.count { tag -> tag[0].jsonPrimitive.content == "imeta" }
                        iMetaTagCount shouldBe expectedAttachments.size
                    },
                )
            }
        }

    /**
     * Hashtags
     */

    @Test
    fun publishShortTextNote_createsHashtagTags_forHashtagsInContent() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val hashtags = listOf("#nostr", "#bicoin", "#primal")
            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "Some note with hashtag $hashtags",
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg {
                        val tTagCount = it.tags.count { tag -> tag[0].jsonPrimitive.content == "t" }
                        tTagCount shouldBe hashtags.size
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_createsHashtagTags_withoutHashSymbol() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val hashtags = listOf("#nostr", "#bicoin", "#primal")
            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "Some note with hashtag $hashtags",
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val tagValues = event.tags.map { tag -> tag[1].jsonPrimitive.content }
                        tagValues shouldBe hashtags.map { it.removePrefix("#") }
                    },
                )
            }
        }

    /**
     * Mentioned users in content.
     */

    @Test
    fun publishShortTextNote_createsPubkeyTags_forMentionedUsers() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val expectedMentionedUser = listOf(
                "nostr:npub13rxpxjc6vh65aay2eswlxejsv0f7530sf64c4arydetpckhfjpustsjeaf",
                "nostr:npub12vkcxr0luzwp8e673v29eqjhrr7p9vqq8asav85swaepclllj09sylpugg",
            )
            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "Hey $expectedMentionedUser, how are you?",
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val pubkeyTagsCount = event.tags.count { tag -> tag[0].jsonPrimitive.content == "p" }
                        pubkeyTagsCount shouldBe expectedMentionedUser.size
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_createsPubkeyTagsForMentionedUsers_withRelayHintsIfAvailable() =
        runTest {
            val expectedEventHints = listOf(
                EventRelayHints(
                    eventId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                    relays = listOf("wss://profile.relay.com"),
                ),
                EventRelayHints(
                    eventId = "532d830dffe09c13e75e8b145c825718fc12b0003f61d61e9077721c7fff93cb",
                    relays = listOf("wss://profile.primal.com"),
                ),
            )
            val expectedMentionedUser = listOf(
                "nostr:npub13rxpxjc6vh65aay2eswlxejsv0f7530sf64c4arydetpckhfjpustsjeaf",
                "nostr:npub12vkcxr0luzwp8e673v29eqjhrr7p9vqq8asav85swaepclllj09sylpugg",
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(eventHints = expectedEventHints),
            )

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "Hey $expectedMentionedUser, how are you?",
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val pubkeyToRelayHintMap = event.tags.map { tag ->
                            tag[1].jsonPrimitive.content to tag[2].jsonPrimitive.content
                        }
                        pubkeyToRelayHintMap.forEach { (pubkey, actualRelayHint) ->
                            val expectedRelay = expectedEventHints.find { it.eventId == pubkey }?.relays?.firstOrNull()
                            actualRelayHint shouldBe expectedRelay
                        }
                    },
                )
            }
        }

    /**
     * Mentioned events (notes, highlights) in content.
     */

    /**
     * Mentioned replaceable events in content.
     */

    /**
     * Resolving proper root tag.
     */

    @Test
    fun publishShortTextNote_createsRootTagForHighlight_evenIfRootArticleAndRootNoteArePresent() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val highlightNevent = buildHighlightNevent()
            val articleNaddr = buildArticleNaddr()
            val noteNevent = buildNoteNevent()

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "I'm replying to a comment on highlight.",
                rootHighlightNevent = highlightNevent,
                rootArticleNaddr = articleNaddr,
                rootNoteNevent = noteNevent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val eventsTag = event.tags.filter { tag -> tag.isEventIdTag() }
                        val rootTagsCount = eventsTag.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagsCount shouldBe 1

                        val rootTag = eventsTag.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTag.shouldNotBeNull()
                        rootTag[0].jsonPrimitive.content shouldBe "e"
                        rootTag[1].jsonPrimitive.content shouldBe highlightNevent.eventId
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_createsRootTagForArticle_evenIfRootNoteIsPresent() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val articleNaddr = buildArticleNaddr()
            val noteNevent = buildNoteNevent()

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "I'm replying to a comment on article.",
                rootHighlightNevent = null,
                rootArticleNaddr = articleNaddr,
                rootNoteNevent = noteNevent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        println(event.tags)
                        val replaceableEventsTags = event.tags.filter { tag -> tag.isATag() }
                        val rootTagCount = replaceableEventsTags.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagCount shouldBe 1

                        val rootTag = replaceableEventsTags.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTag.shouldNotBeNull()
                        rootTag[0].jsonPrimitive.content shouldBe "a"
                        rootTag[1].jsonPrimitive.content shouldBe articleNaddr.asATagValue()
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_createsRootTagForNote_ifRootNoteIsPresentAndNoOtherRootEvents() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val noteNevent = buildNoteNevent()

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "I'm replying to a root note.",
                rootHighlightNevent = null,
                rootArticleNaddr = null,
                rootNoteNevent = noteNevent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val eventsTag = event.tags.filter { tag -> tag.isEventIdTag() }
                        val rootTagsCount = eventsTag.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagsCount shouldBe 1

                        val rootTag = eventsTag.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTag.shouldNotBeNull()
                        rootTag[0].jsonPrimitive.content shouldBe "e"
                        rootTag[1].jsonPrimitive.content shouldBe noteNevent.eventId
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_createsRootTagForNote_ifRootNoteAndReplyToNoteAreTheSame() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val noteNevent = buildNoteNevent()

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "I'm replying to a root note.",
                rootNoteNevent = noteNevent,
                replyToNoteNevent = noteNevent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val eventTags = event.tags.filter { tag -> tag.isEventIdTag() }

                        val rootTagsCount = eventTags.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagsCount shouldBe 1

                        val rootTag = eventTags.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        val replyTag = eventTags.find { tag -> tag[3].jsonPrimitive.content == "reply" }

                        replyTag.shouldBeNull()
                        rootTag.shouldNotBeNull()
                        rootTag[0].jsonPrimitive.content shouldBe "e"
                        rootTag[1].jsonPrimitive.content shouldBe noteNevent.eventId
                    },
                )
            }
        }

    /**
     * Resolve proper reply tag.
     */

    @Test
    fun publishShortTextNote_createsReplyTagForNote_whenRootNoteIsDifferentThanReplyToNote() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val rootNoteNevent = buildNoteNevent(eventId = "rootNote")
            val replyToNoteNevent = buildNoteNevent(eventId = "replyToNote")

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "I'm replying to a reply to root note.",
                rootNoteNevent = rootNoteNevent,
                replyToNoteNevent = replyToNoteNevent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val eventTags = event.tags.filter { tag -> tag.isEventIdTag() }

                        val rootTagsCount = eventTags.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagsCount shouldBe 1
                        val replyTagsCount = eventTags.count { tag -> tag[3].jsonPrimitive.content == "reply" }
                        replyTagsCount shouldBe 1

                        val rootTag = eventTags.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        val replyTag = eventTags.find { tag -> tag[3].jsonPrimitive.content == "reply" }

                        rootTag.shouldNotBeNull()
                        rootTag[0].jsonPrimitive.content shouldBe "e"
                        rootTag[1].jsonPrimitive.content shouldBe rootNoteNevent.eventId

                        replyTag.shouldNotBeNull()
                        replyTag[0].jsonPrimitive.content shouldBe "e"
                        replyTag[1].jsonPrimitive.content shouldBe replyToNoteNevent.eventId
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_createsReplyTagForRootNote_whenArticleRootIsPresent() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val noteNevent = buildNoteNevent()
            val rootArticleNaddr = buildArticleNaddr()

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "I'm replying to a reply to root note.",
                rootNoteNevent = noteNevent,
                replyToNoteNevent = noteNevent,
                rootArticleNaddr = rootArticleNaddr,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val aTags = event.tags.filter { tag -> tag.isATag() }
                        val eventTags = event.tags.filter { tag -> tag.isEventIdTag() }

                        val rootTagsCount = aTags.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagsCount shouldBe 1
                        val replyTagsCount = eventTags.count { tag -> tag[3].jsonPrimitive.content == "reply" }
                        replyTagsCount shouldBe 1

                        val rootATag = aTags.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        val rootEventTag = eventTags.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        val replyTag = eventTags.find { tag -> tag[3].jsonPrimitive.content == "reply" }

                        rootATag.shouldNotBeNull()
                        rootATag[0].jsonPrimitive.content shouldBe "a"
                        rootATag[1].jsonPrimitive.content shouldBe rootArticleNaddr.asATagValue()

                        rootEventTag.shouldBeNull()

                        replyTag.shouldNotBeNull()
                        replyTag[0].jsonPrimitive.content shouldBe "e"
                        replyTag[1].jsonPrimitive.content shouldBe noteNevent.eventId
                    },
                )
            }
        }

    @Test
    fun publishShortTextNote_createsReplyTagForRootNote_whenArticleAndHighlightRootIsPresent() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val noteNevent = buildNoteNevent()
            val rootArticleNaddr = buildArticleNaddr()
            val rootHighlightNevent = buildHighlightNevent()

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "I'm replying to a reply to root note.",
                rootNoteNevent = noteNevent,
                replyToNoteNevent = noteNevent,
                rootArticleNaddr = rootArticleNaddr,
                rootHighlightNevent = rootHighlightNevent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    any(),
                    withArg { event ->
                        val aTags = event.tags.filter { tag -> tag.isATag() }
                        val eventTags = event.tags.filter { tag -> tag.isEventIdTag() }

                        val rootTagsCount = eventTags.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagsCount shouldBe 1
                        val replyTagsCount = eventTags.count { tag -> tag[3].jsonPrimitive.content == "reply" }
                        replyTagsCount shouldBe 1

                        val rootATag = aTags.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        val rootEventTag = eventTags.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        val replyTag = eventTags.find { tag -> tag[3].jsonPrimitive.content == "reply" }

                        rootATag.shouldBeNull()

                        rootEventTag.shouldNotBeNull()
                        rootEventTag[0].jsonPrimitive.content shouldBe "e"
                        rootEventTag[1].jsonPrimitive.content shouldBe rootHighlightNevent.eventId

                        replyTag.shouldNotBeNull()
                        replyTag[0].jsonPrimitive.content shouldBe "e"
                        replyTag[1].jsonPrimitive.content shouldBe noteNevent.eventId
                    },
                )
            }
        }

    /**
     * Referenced pubkey tag testing
     */

    @Test
    fun publishShortTextNote_copiesAllPubkeyTags_fromReplyToNote() =
        runTest {
        }
//    @Test
//    fun `constructPubkeyTags keeps existing pubkey tags`() {
//        val notePublisher = buildNotePublishHandler()
//
//        val existingTags = listOf("test".asPubkeyTag())
//        val replyPostData = buildPostData(
//            tags = existingTags,
//        )
//        val replyToAuthorId = "some author id"
//        val replyToAuthorPubkey = replyToAuthorId.asPubkeyTag()
//
//        val expectedSet = setOf(
//            existingTags,
//            listOf(replyToAuthorPubkey),
//        ).flatten()
//
//        mockkStatic(JsonArray::isPubKeyTag)
//
//        val actualSet = notePublisher.getConstructPubkeyTags()
//            .invoke(
//                notePublisher,
//                replyPostData,
//                replyToAuthorId,
//                null,
//                null,
//            )
//
//        actualSet shouldBe expectedSet
//    }

    @Test
    fun `publishShortTextNote publish new note with single user mentioned`() =
        runTest {
            val mentionedUserNpub = "nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr"
            val mentionedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content $mentionedUserNpub"
            val expectedTags = listOf<JsonArray>(
                mentionedUserId.asPubkeyTag(optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with multiple users mentioned`() =
        runTest {
            val firstMentionedUserNpub = "nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr"
            val secondMentionedUserNpub = "nostr:npub13rxpxjc6vh65aay2eswlxejsv0f7530sf64c4arydetpckhfjpustsjeaf"

            val firstMentionedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"
            val secondMentionedUserId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content $firstMentionedUserNpub $secondMentionedUserNpub"
            val expectedTags = listOf<JsonArray>(
                firstMentionedUserId.asPubkeyTag(optional = "mention"),
                secondMentionedUserId.asPubkeyTag(optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single user mentioned multiple times`() =
        runTest {
            val mentionedUserNpub = "nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr"

            val mentionedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content $mentionedUserNpub $mentionedUserNpub"
            val expectedTags = listOf<JsonArray>(
                mentionedUserId.asPubkeyTag(optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single quoted note`() =
        runTest {
            val quotedNoteUri = "nostr:note1x0zvuxjz8fjagqlssu0gl5npnas67475tqcqsrzs56yk2625f3as6f0rpt"
            val quotedNoteId = "33c4ce1a423a65d403f0871e8fd2619f61af57d45830080c50a6896569544c7b"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content $quotedNoteUri"
            val expectedTags = listOf<JsonArray>(
                quotedNoteId.asEventIdTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with multiple quoted notes`() =
        runTest {
            val firstQuotedNoteUri = "nostr:note1x0zvuxjz8fjagqlssu0gl5npnas67475tqcqsrzs56yk2625f3as6f0rpt"
            val firstQuotedNoteId = "33c4ce1a423a65d403f0871e8fd2619f61af57d45830080c50a6896569544c7b"

            val secondQuotedNoteUri = "nostr:note1e3xjcz3euea8hc3m32qegfgs9xl4rtp80cyy3vxgxvjkrjxe0hwqcfqf35"
            val secondQuotedNoteId = "cc4d2c0a39e67a7be23b8a8194251029bf51ac277e0848b0c8332561c8d97ddc"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content $firstQuotedNoteUri $secondQuotedNoteUri"
            val expectedTags = listOf<JsonArray>(
                firstQuotedNoteId.asEventIdTag(marker = "mention"),
                secondQuotedNoteId.asEventIdTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single quoted note repeating`() =
        runTest {
            val quotedNoteUri = "nostr:note1x0zvuxjz8fjagqlssu0gl5npnas67475tqcqsrzs56yk2625f3as6f0rpt"
            val quotedNoteId = "33c4ce1a423a65d403f0871e8fd2619f61af57d45830080c50a6896569544c7b"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content $quotedNoteUri $quotedNoteUri"
            val expectedTags = listOf<JsonArray>(
                quotedNoteId.asEventIdTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single quoted article`() =
        runTest {
            val naddr = Naddr(
                kind = NostrEventKind.LongFormContent.value,
                userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                identifier = "Testing-Testing-Testing-8yw62n",
                relays = emptyList(),
            )

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content ${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with multiple quoted articles`() =
        runTest {
            val firstNaddr = Naddr(
                kind = NostrEventKind.LongFormContent.value,
                userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                identifier = "Testing-Testing-Testing-8yw62n",
                relays = emptyList(),
            )
            val secondNaddr = Naddr(
                kind = NostrEventKind.LongFormContent.value,
                userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                identifier = "1718785686085",
                relays = emptyList(),
            )

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content ${firstNaddr.toNaddrString()} ${secondNaddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                firstNaddr.asReplaceableEventTag(marker = "mention"),
                secondNaddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single quoted article repeating`() =
        runTest {
            val naddr = Naddr(
                kind = NostrEventKind.LongFormContent.value,
                userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                identifier = "Testing-Testing-Testing-8yw62n",
                relays = emptyList(),
            )

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content ${naddr.toNaddrString()} ${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single quoted highlight and article`() =
        runTest {
            val highlightNevent = Nevent(
                kind = NostrEventKind.Highlight.value,
                userId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a",
                eventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            )

            val naddr = Naddr(
                kind = NostrEventKind.LongFormContent.value,
                userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                identifier = "Testing-Testing-Testing-8yw62n",
                relays = emptyList(),
            )

            val expectedUserId = "someUserId"
            val expectedContent =
                "some simple content nostr:${highlightNevent.toNeventString()} nostr:${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                highlightNevent.asEventTag(marker = "mention"),
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with multiple quoted highlights and article`() =
        runTest {
            val firstHighlightNevent = Nevent(
                kind = NostrEventKind.Highlight.value,
                userId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a",
                eventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            )
            val secondHighlightNevent = Nevent(
                kind = NostrEventKind.Highlight.value,
                userId = "4e616576651c0fa97f437f7be539865245ff27a23aac0b8cc2d77b0e43c4fee8",
                eventId = "d42412c2566f2bd6913938a46518f481259a0cbe0fbfbc9526119b1655733116",
            )

            val naddr = Naddr(
                kind = NostrEventKind.LongFormContent.value,
                userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                identifier = "Testing-Testing-Testing-8yw62n",
                relays = emptyList(),
            )

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content" +
                " nostr:${firstHighlightNevent.toNeventString()}" +
                " nostr:${secondHighlightNevent.toNeventString()} nostr:${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                firstHighlightNevent.asEventTag(marker = "mention"),
                secondHighlightNevent.asEventTag(marker = "mention"),
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single quoted highlight and article repeating`() =
        runTest {
            val highlightNevent = Nevent(
                kind = NostrEventKind.Highlight.value,
                userId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a",
                eventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            )

            val naddr = Naddr(
                kind = NostrEventKind.LongFormContent.value,
                userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                identifier = "Testing-Testing-Testing-8yw62n",
                relays = emptyList(),
            )

            val expectedUserId = "someUserId"
            val expectedContent =
                "some simple content nostr:${highlightNevent.toNeventString()} " +
                    "nostr:${highlightNevent.toNeventString()} ${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                highlightNevent.asEventTag(marker = "mention"),
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkPrimalDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it shouldBe expectedUserId },
                    withArg {
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                )
            }
        }
}
