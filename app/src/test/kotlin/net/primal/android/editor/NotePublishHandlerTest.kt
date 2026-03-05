package net.primal.android.editor

import android.net.Uri
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
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
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.PollOption
import net.primal.android.editor.domain.PollPublishRequest
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.db.Relay as RelayPO
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.repository.RelayRepository
import net.primal.domain.events.EventRelayHints
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.Nip19TLV.toNeventString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asEventTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.asReplaceableEventTag
import net.primal.domain.nostr.isATag
import net.primal.domain.nostr.isEventIdTag
import net.primal.domain.nostr.isPubKeyTag
import net.primal.domain.posts.FeedPost
import net.primal.domain.posts.FeedPostAuthor
import net.primal.domain.posts.FeedRepository
import org.junit.Rule
import org.junit.Test

@Suppress("LargeClass", "MaxLineLength")
@OptIn(ExperimentalCoroutinesApi::class)
class NotePublishHandlerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val expectedUserId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079"

    // region Helpers

    private fun buildNotePublishHandler(
        nostrPublisher: NostrPublisher = mockk(relaxed = true),
        eventRelayHintsRepository: EventRelayHintsRepository = mockk {
            coEvery { findRelaysByIds(any()) } returns emptyList()
        },
        feedRepository: FeedRepository = mockk {
            coEvery { findPostsById(any()) } returns null
        },
        relayRepository: RelayRepository = mockk {
            every { findRelays(any(), any()) } returns emptyList()
        },
    ) = NotePublishHandler(
        dispatcherProvider = coroutinesTestRule.dispatcherProvider,
        nostrPublisher = nostrPublisher,
        eventRelayHintsRepository = eventRelayHintsRepository,
        feedRepository = feedRepository,
        relayRepository = relayRepository,
    )

    private fun buildFeedPost(
        postId: String = UUID.randomUUID().toString(),
        authorId: String = UUID.randomUUID().toString(),
        tags: List<JsonArray> = emptyList(),
        content: String = "",
    ) = FeedPost(
        eventId = postId,
        author = FeedPostAuthor(authorId = authorId, handle = "", displayName = ""),
        content = content,
        tags = tags,
        timestamp = kotlinx.datetime.Instant.fromEpochSeconds(0),
        rawNostrEvent = "",
    )

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

    private fun buildWriteRelayPOs(userId: String, vararg urls: String) =
        urls.map { url ->
            RelayPO(userId = userId, kind = RelayKind.UserRelay, url = url, read = true, write = true)
        }

    // endregion

    // region User IDs

    @Test
    fun publishShortTextNote_authorsNostrEvent_withGivenUserId() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(userId = expectedUserId, content = "")

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it.pubKey shouldBe expectedUserId },
                    any(),
                )
            }
        }

    // endregion

    // region Content & Attachments

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
                    withArg { it.content shouldBe givenContent },
                    any(),
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
                    withArg {
                        it.content shouldStartWith givenContent
                        it.content shouldEndWith expectedRemoteUrl
                    },
                    any(),
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
                    withArg {
                        it.content shouldStartWith givenContent
                        it.content shouldEndWith expectedUrlsAppendix
                    },
                    any(),
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
                    withArg {
                        val appendix = it.content.substringAfter(givenContent)
                        val breakLines = appendix.substring(startIndex = 0, endIndex = 3)
                        breakLines[0] shouldBe '\n'
                        breakLines[1] shouldBe '\n'
                        breakLines[2] shouldNotBe '\n'
                    },
                    any(),
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
                    withArg {
                        val iMetaTagCount = it.tags.count { tag -> tag[0].jsonPrimitive.content == "imeta" }
                        iMetaTagCount shouldBe expectedAttachments.size
                    },
                    any(),
                )
            }
        }

    // endregion

    // region Hashtags

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
                    withArg {
                        val tTagCount = it.tags.count { tag -> tag[0].jsonPrimitive.content == "t" }
                        tTagCount shouldBe hashtags.size
                    },
                    any(),
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
                    withArg { event ->
                        val tagValues = event.tags.map { tag -> tag[1].jsonPrimitive.content }
                        tagValues shouldBe hashtags.map { it.removePrefix("#") }
                    },
                    any(),
                )
            }
        }

    // endregion

    // region Mentioned users in content

    @Test
    fun publishShortTextNote_addsWhitespaceBefore_forMentionedUsersWithoutWhitespace() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val input = "Heynostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr how are you?"
            val expectedOutput = "Hey nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr " +
                "how are you?"

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = input,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event -> event.content shouldBe expectedOutput },
                    any(),
                )
            }
        }

    @Test
    fun publishShortTextNote_handlesMultipleMentionsCorrectly_forMultipleMentionedUsersWithoutWhitespace() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val input = "nostr:npub1235jkvq3mr0pm7gpqkj07n4lw2yacr009h0ncqgegrt00s3hugdsmpzphwnostr:" +
                "npub1235jkvq3mr0pm7gpqkj07n4lw2yacr009h0ncqgegrt00s3hugdsmpzphw"
            val expectedOutput = "nostr:npub1235jkvq3mr0pm7gpqkj07n4lw2yacr009h0ncqgegrt00s3hugdsmpzphw " +
                "nostr:npub1235jkvq3mr0pm7gpqkj07n4lw2yacr009h0ncqgegrt00s3hugdsmpzphw"

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = input,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event -> event.content shouldBe expectedOutput },
                    any(),
                )
            }
        }

    @Test
    fun publishShortTextNote_doesNotModifyCorrectlyFormattedMentions_forMentionedUsersWithWhitespace() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val content = "Hey nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr, how are you? " +
                "Check this nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr's note!"

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = content,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event -> event.content shouldBe content },
                    any(),
                )
            }
        }

    @Test
    fun publishShortTextNote_correctlyProcessesTextWithoutMentions_withoutMentionedUsers() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val content = "This is a normal message without any npub mentions."

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = content,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event -> event.content shouldBe content },
                    any(),
                )
            }
        }

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
                    withArg { event ->
                        val pubkeyTagsCount = event.tags.count { tag -> tag[0].jsonPrimitive.content == "p" }
                        pubkeyTagsCount shouldBe expectedMentionedUser.size
                    },
                    any(),
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
            val eventRelayHintsRepository = mockk<EventRelayHintsRepository> {
                coEvery { findRelaysByIds(any()) } returns expectedEventHints
            }
            val notePublishHandler = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                eventRelayHintsRepository = eventRelayHintsRepository,
            )

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = "Hey $expectedMentionedUser, how are you?",
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val pubkeyToRelayHintMap = event.tags.map { tag ->
                            tag[1].jsonPrimitive.content to tag[2].jsonPrimitive.content
                        }
                        pubkeyToRelayHintMap.forEach { (pubkey, actualRelayHint) ->
                            val expectedRelay = expectedEventHints.find { it.eventId == pubkey }?.relays?.firstOrNull()
                            actualRelayHint shouldBe expectedRelay
                        }
                    },
                    any(),
                )
            }
        }

    // endregion

    // region Mentioned events (notes, highlights) in content

    @Test
    fun `publishShortTextNote publish new note with single quoted note`() =
        runTest {
            val quotedNoteUri = "nostr:note1x0zvuxjz8fjagqlssu0gl5npnas67475tqcqsrzs56yk2625f3as6f0rpt"
            val quotedNoteId = "33c4ce1a423a65d403f0871e8fd2619f61af57d45830080c50a6896569544c7b"

            val expectedContent = "some simple content $quotedNoteUri"
            val expectedTags = quotedNoteId.asEventIdTag(marker = "mention")

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldContain expectedTags
                    },
                    any(),
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

            val expectedContent = "some simple content $firstQuotedNoteUri $secondQuotedNoteUri"
            val expectedTags = listOf<JsonArray>(
                firstQuotedNoteId.asEventIdTag(marker = "mention"),
                secondQuotedNoteId.asEventIdTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldContainAll expectedTags
                    },
                    any(),
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single quoted note repeating`() =
        runTest {
            val quotedNoteUri = "nostr:note1x0zvuxjz8fjagqlssu0gl5npnas67475tqcqsrzs56yk2625f3as6f0rpt"
            val quotedNoteId = "33c4ce1a423a65d403f0871e8fd2619f61af57d45830080c50a6896569544c7b"

            val expectedContent = "some simple content $quotedNoteUri $quotedNoteUri"
            val expectedTags = listOf<JsonArray>(
                quotedNoteId.asEventIdTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                    any(),
                )
            }
        }

    // endregion

    // region Mentioned replaceable events in content

    @Test
    fun `publishShortTextNote publish new note with single quoted article`() =
        runTest {
            val naddr = Naddr(
                kind = NostrEventKind.LongFormContent.value,
                userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                identifier = "Testing-Testing-Testing-8yw62n",
                relays = emptyList(),
            )

            val expectedContent = "some simple content ${naddr.toNaddrString()}"
            val expectedTag = naddr.asReplaceableEventTag(marker = "mention")

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldContain expectedTag
                    },
                    any(),
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

            val expectedContent = "some simple content ${firstNaddr.toNaddrString()} ${secondNaddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                firstNaddr.asReplaceableEventTag(marker = "mention"),
                secondNaddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldContainAll expectedTags
                    },
                    any(),
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

            val expectedContent = "some simple content ${naddr.toNaddrString()} ${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                    any(),
                )
            }
        }

    // endregion

    // region Resolving proper root tag

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
                    withArg { event ->
                        val eventsTag = event.tags.filter { tag -> tag.isEventIdTag() }
                        val rootTagsCount = eventsTag.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagsCount shouldBe 1

                        val rootTag = eventsTag.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTag.shouldNotBeNull()
                        rootTag[0].jsonPrimitive.content shouldBe "e"
                        rootTag[1].jsonPrimitive.content shouldBe highlightNevent.eventId
                    },
                    any(),
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
                    withArg { event ->
                        val replaceableEventsTags = event.tags.filter { tag -> tag.isATag() }
                        val rootTagCount = replaceableEventsTags.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagCount shouldBe 1

                        val rootTag = replaceableEventsTags.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTag.shouldNotBeNull()
                        rootTag[0].jsonPrimitive.content shouldBe "a"
                        rootTag[1].jsonPrimitive.content shouldBe articleNaddr.asATagValue()
                    },
                    any(),
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
                    withArg { event ->
                        val eventsTag = event.tags.filter { tag -> tag.isEventIdTag() }
                        val rootTagsCount = eventsTag.count { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTagsCount shouldBe 1

                        val rootTag = eventsTag.find { tag -> tag[3].jsonPrimitive.content == "root" }
                        rootTag.shouldNotBeNull()
                        rootTag[0].jsonPrimitive.content shouldBe "e"
                        rootTag[1].jsonPrimitive.content shouldBe noteNevent.eventId
                    },
                    any(),
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
                    any(),
                )
            }
        }

    // endregion

    // region Resolve proper reply tag

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
                    any(),
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
                    any(),
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
                    any(),
                )
            }
        }

    // endregion

    // region Referenced pubkey tags

    @Test
    fun publishShortTextNote_copiesAllPubkeyTags_fromReplyToNote() =
        runTest {
            val existingTag = "test".asPubkeyTag()
            val replyEventId = "replyEventId"
            val replyAuthorId = "some author id"
            val feedPost = buildFeedPost(
                postId = replyEventId,
                authorId = replyAuthorId,
                tags = listOf(existingTag),
            )
            val replyNevent = Nevent(
                kind = NostrEventKind.ShortTextNote.value,
                userId = replyAuthorId,
                eventId = replyEventId,
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val feedRepository = mockk<FeedRepository> {
                coEvery { findPostsById(replyEventId) } returns feedPost
            }
            val notePublishHandler = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                feedRepository = feedRepository,
            )

            notePublishHandler.publishShortTextNote(
                userId = "some user id",
                content = "some content",
                replyToNoteNevent = replyNevent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event -> event.tags shouldContain existingTag },
                    any(),
                )
            }
        }

    // endregion

    // region Mentioned user tags

    @Test
    fun `publishShortTextNote publish new note with single user mentioned`() =
        runTest {
            val mentionedUserNpub = "nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr"
            val mentionedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"

            val expectedContent = "some simple content $mentionedUserNpub"
            val expectedTags = listOf<JsonArray>(
                mentionedUserId.asPubkeyTag(optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                    any(),
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

            val expectedContent = "some simple content $firstMentionedUserNpub $secondMentionedUserNpub"
            val expectedTags = listOf<JsonArray>(
                firstMentionedUserId.asPubkeyTag(optional = "mention"),
                secondMentionedUserId.asPubkeyTag(optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                    any(),
                )
            }
        }

    @Test
    fun `publishShortTextNote publish new note with single user mentioned multiple times`() =
        runTest {
            val mentionedUserNpub = "nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr"

            val mentionedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"

            val expectedContent = "some simple content $mentionedUserNpub $mentionedUserNpub"
            val expectedTags = listOf<JsonArray>(
                mentionedUserId.asPubkeyTag(optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                    any(),
                )
            }
        }

    // endregion

    // region Mentioned highlights and articles in content

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

            val expectedContent =
                "some simple content nostr:${highlightNevent.toNeventString()} nostr:${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                highlightNevent.asEventTag(marker = "mention"),
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldContainAll expectedTags
                    },
                    any(),
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

            val expectedContent = "some simple content" +
                " nostr:${firstHighlightNevent.toNeventString()}" +
                " nostr:${secondHighlightNevent.toNeventString()} nostr:${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                firstHighlightNevent.asEventTag(marker = "mention"),
                secondHighlightNevent.asEventTag(marker = "mention"),
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldContainAll expectedTags
                    },
                    any(),
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

            val expectedContent =
                "some simple content nostr:${highlightNevent.toNeventString()} " +
                    "nostr:${highlightNevent.toNeventString()} ${naddr.toNaddrString()}"
            val expectedTags = listOf<JsonArray>(
                highlightNevent.asEventTag(marker = "mention"),
                naddr.asReplaceableEventTag(marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg {
                        it.pubKey shouldBe expectedUserId
                        it.content shouldBe expectedContent
                        it.tags shouldBe expectedTags
                    },
                    any(),
                )
            }
        }

    // endregion

    // region User Poll tests

    @Test
    fun publishPoll_usesCorrectEventKind_forUserPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Which is better?",
                pollRequest = PollPublishRequest(
                    isZapPoll = false,
                    choices = listOf(
                        PollOption(id = "1", label = "Option A"),
                        PollOption(id = "2", label = "Option B"),
                    ),
                    endsAt = 1700000000,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it.kind shouldBe NostrEventKind.Poll.value },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_usesCorrectEventKind_forZapPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Which is better?",
                pollRequest = PollPublishRequest(
                    isZapPoll = true,
                    choices = listOf(
                        PollOption(id = "1", label = "Option A"),
                        PollOption(id = "2", label = "Option B"),
                    ),
                    endsAt = 1700000000,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { it.kind shouldBe NostrEventKind.ZapPoll.value },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsOptionTags_forUserPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val choices = listOf(
                PollOption(id = "opt1", label = "First choice"),
                PollOption(id = "opt2", label = "Second choice"),
                PollOption(id = "opt3", label = "Third choice"),
            )

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Pick one",
                pollRequest = PollPublishRequest(isZapPoll = false, choices = choices, endsAt = 1700000000),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val optionTags = event.tags.filter { it[0].jsonPrimitive.content == "option" }
                        optionTags.size shouldBe choices.size
                        choices.forEach { choice ->
                            val tag = optionTags.find { it[1].jsonPrimitive.content == choice.id }
                            tag.shouldNotBeNull()
                            tag[2].jsonPrimitive.content shouldBe choice.label
                        }
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsPollTypeTag_forUserPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Pick one",
                pollRequest = PollPublishRequest(
                    isZapPoll = false,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val pollTypeTag = event.tags.find { it[0].jsonPrimitive.content == "polltype" }
                        pollTypeTag.shouldNotBeNull()
                        pollTypeTag[1].jsonPrimitive.content shouldBe "singlechoice"
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsEndsAtTag_forUserPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val endsAt = 1700000000L

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Pick one",
                pollRequest = PollPublishRequest(
                    isZapPoll = false,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = endsAt,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val endsAtTag = event.tags.find { it[0].jsonPrimitive.content == "endsAt" }
                        endsAtTag.shouldNotBeNull()
                        endsAtTag[1].jsonPrimitive.content shouldBe endsAt.toString()
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsRelayTags_forUserPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val relayUrls = listOf("wss://relay1.example.com", "wss://relay2.example.com")
            val relayRepository = mockk<RelayRepository> {
                every { findRelays(any(), any()) } returns buildWriteRelayPOs(expectedUserId, *relayUrls.toTypedArray())
            }
            val notePublishHandler = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                relayRepository = relayRepository,
            )

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Pick one",
                pollRequest = PollPublishRequest(
                    isZapPoll = false,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val relayTags = event.tags.filter { it[0].jsonPrimitive.content == "relay" }
                        relayTags.size shouldBe relayUrls.size
                        relayUrls.forEach { url ->
                            relayTags.any { it[1].jsonPrimitive.content == url } shouldBe true
                        }
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsNoRelayTags_whenNoWriteRelaysAvailable() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Pick one",
                pollRequest = PollPublishRequest(
                    isZapPoll = false,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val relayTags = event.tags.filter { it[0].jsonPrimitive.content == "relay" }
                        relayTags.size shouldBe 0
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_includesContentAndPreparedTags_forUserPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val content = "What do you think? #poll"

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = content,
                pollRequest = PollPublishRequest(
                    isZapPoll = false,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        event.content shouldBe content
                        event.pubKey shouldBe expectedUserId
                        val hashtagTags = event.tags.filter { it[0].jsonPrimitive.content == "t" }
                        hashtagTags.size shouldBe 1
                    },
                    any(),
                )
            }
        }

    // endregion

    // region Zap Poll tests

    @Test
    fun publishPoll_createsPollOptionTags_forZapPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val choices = listOf(
                PollOption(id = "ignored-id-1", label = "First choice"),
                PollOption(id = "ignored-id-2", label = "Second choice"),
                PollOption(id = "ignored-id-3", label = "Third choice"),
            )

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Zap your pick",
                pollRequest = PollPublishRequest(isZapPoll = true, choices = choices, endsAt = 1700000000),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val pollOptionTags = event.tags.filter { it[0].jsonPrimitive.content == "poll_option" }
                        pollOptionTags.size shouldBe choices.size
                        choices.forEachIndexed { index, choice ->
                            val tag = pollOptionTags[index]
                            tag[1].jsonPrimitive.content shouldBe index.toString()
                            tag[2].jsonPrimitive.content shouldBe choice.label
                        }
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsPTagWithRelay_forZapPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val firstRelayUrl = "wss://relay1.example.com"
            val relayRepository = mockk<RelayRepository> {
                every { findRelays(any(), any()) } returns buildWriteRelayPOs(
                    expectedUserId,
                    firstRelayUrl,
                    "wss://relay2.example.com",
                )
            }
            val notePublishHandler = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                relayRepository = relayRepository,
            )

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Zap poll",
                pollRequest = PollPublishRequest(
                    isZapPoll = true,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val pTags = event.tags.filter { it.isPubKeyTag() }
                        val pollPTag = pTags.find { it[1].jsonPrimitive.content == expectedUserId }
                        pollPTag.shouldNotBeNull()
                        pollPTag[2].jsonPrimitive.content shouldBe firstRelayUrl
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsPTagWithoutRelay_forZapPollWithNoWriteRelays() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Zap poll",
                pollRequest = PollPublishRequest(
                    isZapPoll = true,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val pTags = event.tags.filter { it.isPubKeyTag() }
                        val pollPTag = pTags.find { it[1].jsonPrimitive.content == expectedUserId }
                        pollPTag.shouldNotBeNull()
                        pollPTag.size shouldBe 2
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsClosedAtTag_forZapPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val endsAt = 1700000000L

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Zap poll",
                pollRequest = PollPublishRequest(
                    isZapPoll = true,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = endsAt,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val closedAtTag = event.tags.find { it[0].jsonPrimitive.content == "closed_at" }
                        closedAtTag.shouldNotBeNull()
                        closedAtTag[1].jsonPrimitive.content shouldBe endsAt.toString()
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsValueMinimumTag_forZapPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val minSats = 100L

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Zap poll",
                pollRequest = PollPublishRequest(
                    isZapPoll = true,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                    minZapAmountInSats = minSats,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val valueMinTag = event.tags.find { it[0].jsonPrimitive.content == "value_minimum" }
                        valueMinTag.shouldNotBeNull()
                        valueMinTag[1].jsonPrimitive.content shouldBe minSats.toString()
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_createsValueMaximumTag_forZapPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            val maxSats = 10000L

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Zap poll",
                pollRequest = PollPublishRequest(
                    isZapPoll = true,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                    maxZapAmountInSats = maxSats,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val valueMaxTag = event.tags.find { it[0].jsonPrimitive.content == "value_maximum" }
                        valueMaxTag.shouldNotBeNull()
                        valueMaxTag[1].jsonPrimitive.content shouldBe maxSats.toString()
                    },
                    any(),
                )
            }
        }

    @Test
    fun publishPoll_omitsMinMaxTags_whenNull_forZapPoll() =
        runTest {
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublishHandler = buildNotePublishHandler(nostrPublisher = nostrPublisher)

            notePublishHandler.publishPoll(
                userId = expectedUserId,
                content = "Zap poll",
                pollRequest = PollPublishRequest(
                    isZapPoll = true,
                    choices = listOf(PollOption(id = "1", label = "A")),
                    endsAt = 1700000000,
                    minZapAmountInSats = null,
                    maxZapAmountInSats = null,
                ),
            )

            coVerify {
                nostrPublisher.signPublishImportNostrEvent(
                    withArg { event ->
                        val valueMinTag = event.tags.find { it[0].jsonPrimitive.content == "value_minimum" }
                        val valueMaxTag = event.tags.find { it[0].jsonPrimitive.content == "value_maximum" }
                        valueMinTag.shouldBeNull()
                        valueMaxTag.shouldBeNull()
                    },
                    any(),
                )
            }
        }

    // endregion
}
