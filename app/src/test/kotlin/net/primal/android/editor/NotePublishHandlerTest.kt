package net.primal.android.editor

import android.net.Uri
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.lang.reflect.Method
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.nostr.db.EventRelayHints
import net.primal.android.nostr.db.EventRelayHintsDao
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nevent
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import net.primal.android.nostr.utils.Nip19TLV.toNeventString
import net.primal.android.notes.db.PostDao
import net.primal.android.notes.db.PostData
import org.junit.Rule
import org.junit.Test

@Suppress("LargeClass")
@OptIn(ExperimentalCoroutinesApi::class)
class NotePublishHandlerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @Test
    fun `publishShortTextNote publish new note`() =
        runTest {
            val expectedUserId = "someUserId"
            val expectedContent = "some simple content"
            val expectedTags = emptyList<JsonArray>()

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
    fun `publishShortTextNote publish reply to note`() =
        runTest {
            val rootPostId = "somePostId"
            val replyToAuthorId = "someAuthorId"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content"
            val expectedTags = listOf<JsonArray>(
                replyToAuthorId.asPubkeyTag(),
                rootPostId.asEventIdTag(marker = "root"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
                rootPostId = rootPostId,
                replyToAuthorId = replyToAuthorId,
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
    fun `publishShortTextNote publish reply to reply to note`() =
        runTest {
            val rootPostId = "somePostId"
            val replyToPostId = "idOfTheNoteWeAreReplying"
            val replyToAuthorId = "someAuthorId"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content"
            val expectedTags = listOf<JsonArray>(
                replyToAuthorId.asPubkeyTag(),
                rootPostId.asEventIdTag(marker = "root"),
                replyToPostId.asEventIdTag(marker = "reply"),
            )

            val replyPostData = buildPostData(postId = replyToPostId, authorId = replyToAuthorId)

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(post = replyPostData),
            )

            notePublisher.publishShortTextNote(
                userId = expectedUserId,
                content = expectedContent,
                rootPostId = rootPostId,
                replyToPostId = replyToPostId,
                replyToAuthorId = replyToAuthorId,
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
    fun `publishShortTextNote publish new note with single user mentioned`() =
        runTest {
            val mentionedUserNpub = "nostr:npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr"
            val mentionedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"

            val expectedUserId = "someUserId"
            val expectedContent = "some simple content $mentionedUserNpub"
            val expectedTags = listOf<JsonArray>(
                mentionedUserId.asPubkeyTag(relayHint = "", optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                firstMentionedUserId.asPubkeyTag(relayHint = "", optional = "mention"),
                secondMentionedUserId.asPubkeyTag(relayHint = "", optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                mentionedUserId.asPubkeyTag(relayHint = "", optional = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                quotedNoteId.asEventIdTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                firstQuotedNoteId.asEventIdTag(relayHint = "", marker = "mention"),
                secondQuotedNoteId.asEventIdTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                quotedNoteId.asEventIdTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                naddr.asReplaceableEventTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                firstNaddr.asReplaceableEventTag(relayHint = "", marker = "mention"),
                secondNaddr.asReplaceableEventTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                naddr.asReplaceableEventTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                highlightNevent.eventId.asEventIdTag(relayHint = "", marker = "mention"),
                naddr.asReplaceableEventTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                firstHighlightNevent.eventId.asEventIdTag(marker = "mention"),
                secondHighlightNevent.eventId.asEventIdTag(marker = "mention"),
                naddr.asReplaceableEventTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
                highlightNevent.eventId.asEventIdTag(marker = "mention"),
                naddr.asReplaceableEventTag(relayHint = "", marker = "mention"),
            )

            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val notePublisher = buildNotePublishHandler(
                nostrPublisher = nostrPublisher,
                database = mockkDatabase(),
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
            .invoke(notePublisher, "test", "test").shouldBeNull()
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
            rootHighlightId.asEventIdTag(marker = "root"),
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
            rootPostId.asEventIdTag(marker = "root"),
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
            rootPostId.asEventIdTag(marker = "root"),
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

    private fun mockkDatabase(
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
