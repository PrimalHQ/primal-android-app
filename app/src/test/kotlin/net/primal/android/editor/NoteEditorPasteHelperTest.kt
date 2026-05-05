package net.primal.android.editor

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.utils.takeAsNeventOrNull
import org.junit.Test

class NoteEditorPasteHelperTest {

    private val validNevent = "nevent1qqs0ejzkvqdlqaej8k7edamkvzcnjv77u6npmp2qhdpv" +
        "swyjvcplafqpp4mhxue69uhkummn9ekx7mqzyrhxagf6h8l9cjngatumrg60uq2" +
        "2v66qz979pm32v985ek54ndh8gqcyqqqqqqgpldx8x"

    private val validNote = "note1zwwmjqpaymqhjlq0tjezcyhpcx0hyh4m3u5p54tm7mmfxaqg03pq3w3d6g"

    private val productionLikeIsEmbeddable: (String) -> Boolean = { uri ->
        val nevent = uri.takeAsNeventOrNull()
        nevent != null && (
            nevent.kind == null ||
                nevent.kind == NostrEventKind.ShortTextNote.value ||
                nevent.kind == NostrEventKind.Highlight.value
            )
    }

    @Test
    fun `bare nevent token returns sourceMatch and canonicalUri equal to the bech32`() {
        val result = validNevent.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe validNevent
        result[0].canonicalUri shouldBe validNevent
    }

    @Test
    fun `nostr-prefixed nevent paste returns the prefix in both sourceMatch and canonicalUri`() {
        val token = "nostr:$validNevent"

        val result = token.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe token
        result[0].canonicalUri shouldBe token
    }

    @Test
    fun `https primal-net URL with nevent path returns full URL as sourceMatch and bare bech32 as canonicalUri`() {
        val url = "https://primal.net/e/$validNevent"

        val result = url.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe url
        result[0].canonicalUri shouldBe validNevent
    }

    @Test
    fun `host-agnostic - works for njump-me with naddr-style token`() {
        val url = "https://njump.me/$validNote"

        val result = url.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe url
        result[0].canonicalUri shouldBe validNote
    }

    @Test
    fun `URL with query string strips the whole URL including query`() {
        val url = "https://primal.net/e/$validNevent?ref=foo"

        val result = url.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe url
        result[0].canonicalUri shouldBe validNevent
    }

    @Test
    fun `URL with trailing period peels the period off sourceMatch`() {
        val url = "https://primal.net/e/$validNevent"
        val text = "$url."

        val result = text.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe url
        result[0].canonicalUri shouldBe validNevent
    }

    @Test
    fun `URL with trailing close paren and period peels both off sourceMatch`() {
        val url = "https://primal.net/e/$validNevent"
        val text = "$url)."

        val result = text.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe url
    }

    @Test
    fun `non-embeddable token returns empty list and the token is preserved`() {
        val text = "https://primal.net/e/$validNevent"

        val result = text.extractEmbeddableNostrTokens(isEmbeddable = { false })

        result.shouldBeEmpty()
    }

    @Test
    fun `mixed input returns only embeddable tokens in source order`() {
        val embeddable = "https://primal.net/e/$validNevent"
        val nonEmbeddable = "https://example.com/p/npub1xxx"
        val plain = "hello"
        val text = "$plain $embeddable $nonEmbeddable"

        val result = text.extractEmbeddableNostrTokens(
            isEmbeddable = { uri -> uri.contains("nevent1") },
        )

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe embeddable
        result[0].canonicalUri shouldBe validNevent
    }

    @Test
    fun `duplicate token in same paste returns one entry per occurrence in source order`() {
        val url = "https://primal.net/e/$validNevent"
        val text = "$url and again $url"

        val result = text.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result shouldHaveSize 2
        result[0].sourceMatch shouldBe url
        result[1].sourceMatch shouldBe url
    }

    @Test
    fun `text without nostr fragments returns empty list`() {
        val text = "hello world this is a plain note"

        val result = text.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result.shouldBeEmpty()
    }

    @Test
    fun `malformed bech32 inside URL returns empty list`() {
        val url = "https://primal.net/e/nevent1zzznotbechzzz"

        val result = url.extractEmbeddableNostrTokens(isEmbeddable = { true })

        result.shouldBeEmpty()
    }

    @Test
    fun `kind-less nevent URL from primal-net is recognized as embeddable`() {
        val keptKindLessNevent =
            "nevent1qqs2vtwlmj6zyss982vc96g8wul4dfacqr90cm2mhu5tqgtmxlgu8acrpuhcf"
        val url = "https://primal.net/e/$keptKindLessNevent"

        val result = url.extractEmbeddableNostrTokens(productionLikeIsEmbeddable)

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe url
        result[0].canonicalUri shouldBe keptKindLessNevent
    }

    @Test
    fun `kind-less nevent URL from yakihonne with relays and author is recognized as embeddable`() {
        val keptKindLessNevent = "nevent1qgsglv2qkn5dmmuhee9cy8fywfu2rfp4xd3xy0myqg2gfvmjl9yqqrqppemhxue69" +
            "uhkummn9ekx7mp0qywhwumn8ghj7mn0wd68ytfsxgh8jcttd95x7mnwv5hxxmmd9uqzqyyd83sgrncgsnfta7t9xg2n8" +
            "puhk0gdy9fn2j9wzrlxjrkgsuxv26hpuy"
        val url = "https://yakihonne.com/note/$keptKindLessNevent"

        val result = url.extractEmbeddableNostrTokens(productionLikeIsEmbeddable)

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe url
        result[0].canonicalUri shouldBe keptKindLessNevent
    }

    @Test
    fun `kind-9802 highlight nevent URL is recognized as embeddable`() {
        val keptKindNevent = "nevent1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35pjhluenmfekd53mqzyrtp7w" +
            "79k045gq80mtnpdxjuzl9t7vjxk52rv80f888y5xsd5mh55qcyqqqzvjsk2whrp"
        val url = "https://primal.net/e/$keptKindNevent"

        val result = url.extractEmbeddableNostrTokens(productionLikeIsEmbeddable)

        result shouldHaveSize 1
        result[0].sourceMatch shouldBe url
        result[0].canonicalUri shouldBe keptKindNevent
    }

    @Test
    fun `production mapUriToReferencedUri equivalent accepts kind-less nevents (regression guard)`() {
        val sampleKindLessNevent =
            "nevent1qqs2vtwlmj6zyss982vc96g8wul4dfacqr90cm2mhu5tqgtmxlgu8acrpuhcf"

        val nevent = sampleKindLessNevent.takeAsNeventOrNull()
        nevent.shouldNotBeNull()
        nevent.kind shouldBe null

        val accepted = nevent.kind == null ||
            nevent.kind == NostrEventKind.ShortTextNote.value ||
            nevent.kind == NostrEventKind.Highlight.value

        accepted shouldBe true
    }
}
