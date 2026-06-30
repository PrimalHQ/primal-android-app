package net.primal.data.local.serialization

import kotlin.test.Test
import kotlin.time.measureTime
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.links.CdnImage
import net.primal.domain.links.CdnResourceVariant
import net.primal.domain.membership.PrimalLegendProfile
import net.primal.domain.membership.PrimalPremiumInfo
import net.primal.domain.nostr.NostrEvent

/**
 * ISOLATED micro-benchmark. Touches no production code; only decodes representative JSON
 * payloads through the SAME `decodeFromJsonStringOrNull` / `CommonJson` path the Room
 * TypeConverters use, to quantify per-page deserialization cost for the note feed.
 *
 * NOTE: runs on the desktop JVM (`:data:caching:local:desktopTest`). JVM JIT makes these
 * numbers OPTIMISTIC versus Android ART/R8 on a real device — treat them as a lower bound
 * and as RELATIVE costs between payload types, not absolute device timings.
 */
class FeedDeserializationCostTest {

    // ---- Representative payloads (built via real constructors, encoded with CommonJson) ----

    private val hex = "a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f90"

    private val tagsJson = """
        [["e","$hex","wss://relay.primal.net","root"],
         ["e","$hex","wss://relay.damus.io","reply"],
         ["p","$hex"],["p","$hex"],["p","$hex"],
         ["t","nostr"],["t","bitcoin"],["t","grownostr"]]
    """.trimIndent()

    private val tags: List<JsonArray> = tagsJson.decodeFromJsonStringOrNull<List<JsonArray>>()!!

    private val content =
        "Any Android users out there? Would you like to try the new Primal Android build? " +
            "We just shipped self-custodial Lightning, nostr:npub1abcdefgh and a bunch of fixes. " +
            "Check it out at https://primal.net #nostr #bitcoin"

    private val rawEventJson: String = NostrEvent(
        id = hex,
        pubKey = hex,
        createdAt = 1_700_000_000L,
        kind = 1,
        tags = tags,
        content = content,
        sig = hex + hex,
    ).encodeToJsonString()

    private fun cdnImage(variants: Int) =
        CdnImage(
            sourceUrl = "https://cdn.primal.net/media/$hex.jpg",
            variants = (0 until variants).map {
                CdnResourceVariant(
                    width = 100 * (it + 1),
                    height = 100 * (it + 1),
                    mediaUrl = "https://cdn.primal.net/media/$hex/$it.jpg",
                )
            },
        )

    private val avatarJson = cdnImage(variants = 4).encodeToJsonString()
    private val bannerJson = cdnImage(variants = 4).encodeToJsonString()
    private val aboutUrisJson = listOf(
        "https://primal.net",
        "https://github.com/primal",
        "https://x.com/primal",
    ).encodeToJsonString()
    private val aboutHashtagsJson = listOf("nostr", "bitcoin", "lightning", "primal").encodeToJsonString()
    private val blossomsJson = listOf("https://blossom.primal.net", "https://cdn.satellite.earth").encodeToJsonString()
    private val premiumJson = PrimalPremiumInfo(
        primalName = "alice",
        tier = "premium",
        expiresAt = 1_800_000_000L,
        legendSince = 1_600_000_000L,
        legendProfile = PrimalLegendProfile(
            styleId = "gold",
            customBadge = true,
            avatarGlow = true,
            inLeaderboard = true,
        ),
    ).encodeToJsonString()

    // ProfileData.raw is itself a full (kind-0 metadata) Nostr event JSON.
    private val profileRawJson: String = NostrEvent(
        id = hex,
        pubKey = hex,
        createdAt = 1_700_000_000L,
        kind = 0,
        tags = emptyList(),
        content = """{"name":"alice","about":"building on nostr","picture":"https://cdn.primal.net/$hex.jpg"}""",
        sig = hex + hex,
    ).encodeToJsonString()

    // ---- Bench harness ----

    private inline fun bench(
        warmup: Int,
        iterations: Int,
        crossinline block: () -> Int,
    ): Double {
        var sink = 0
        repeat(warmup) { sink += block() }
        val elapsed = measureTime { repeat(iterations) { sink += block() } }
        // Defeat dead-code elimination.
        if (sink == 1 && iterations == 0) println("unreachable $sink")
        return elapsed.inWholeNanoseconds.toDouble() / iterations
    }

    private fun round2(x: Double): String {
        val r = (x * 100.0).toLong() / 100.0
        return r.toString()
    }

    @Test
    fun `measure per-page feed deserialization cost`() {
        val warmup = 20_000
        val iters = 200_000

        val nsRawEvent = bench(warmup, iters) { rawEventJson.decodeFromJsonStringOrNull<NostrEvent>()?.kind ?: 0 }
        val nsTags = bench(warmup, iters) { tagsJson.decodeFromJsonStringOrNull<List<JsonArray>>()?.size ?: 0 }
        val nsAvatar = bench(warmup, iters) { avatarJson.decodeFromJsonStringOrNull<CdnImage>()?.variants?.size ?: 0 }
        val nsBanner = bench(warmup, iters) { bannerJson.decodeFromJsonStringOrNull<CdnImage>()?.variants?.size ?: 0 }
        val nsAboutUris = bench(warmup, iters) { aboutUrisJson.decodeFromJsonStringOrNull<List<String>>()?.size ?: 0 }
        val nsAboutTags =
            bench(warmup, iters) { aboutHashtagsJson.decodeFromJsonStringOrNull<List<String>>()?.size ?: 0 }
        val nsBlossoms = bench(warmup, iters) { blossomsJson.decodeFromJsonStringOrNull<List<String>>()?.size ?: 0 }
        val nsPremium =
            bench(warmup, iters) { premiumJson.decodeFromJsonStringOrNull<PrimalPremiumInfo>()?.tier?.length ?: 0 }
        val nsProfileRaw = bench(warmup, iters) { profileRawJson.decodeFromJsonStringOrNull<NostrEvent>()?.kind ?: 0 }

        // ---- Composition for a single REFRESH page (initialLoadSize = 75 notes) ----
        val pageNotes = 75

        // Main row decodes per note: PostData.raw (rawKind waste) + PostData.tags.
        val nsMainRow = nsRawEvent + nsTags

        // ProfileData JSON columns per profile row (all 6 decoded by the converter on read):
        val nsProfileAll = nsAvatar + nsBanner + nsAboutUris + nsAboutTags + nsBlossoms + nsPremium
        // ...of which the feed mapper actually USES only avatar + premium(legend) + blossoms:
        val nsProfileUsed = nsAvatar + nsPremium + nsBlossoms
        // ...and these are pure waste in the feed path (parsed then discarded):
        val nsProfileWasted = nsBanner + nsAboutUris + nsAboutTags

        // Per-note, author profile only (x1). Real aggregate loads ProfileData up to x3
        // (author + repostAuthor + replyToAuthor).
        val nsNoteX1 = nsMainRow + nsProfileAll
        val nsNoteX3 = nsMainRow + (nsProfileAll * 3)

        val msPageX1 = nsNoteX1 * pageNotes / 1_000_000.0
        val msPageX3 = nsNoteX3 * pageNotes / 1_000_000.0
        val msWastedPage = nsProfileWasted * pageNotes / 1_000_000.0
        val msRawKindPage = nsRawEvent * pageNotes / 1_000_000.0
        val msProfileRawPage = nsProfileRaw * pageNotes / 1_000_000.0

        val report = buildString {
            appendLine()
            appendLine("================ FEED DESERIALIZATION COST (desktop JVM; optimistic) ================")
            appendLine("Per-decode cost (nanoseconds/op, $iters iters after $warmup warmup):")
            appendLine("  PostData.raw -> NostrEvent (rawKind waste) : ${round2(nsRawEvent)} ns")
            appendLine("  PostData.tags -> List<JsonArray>           : ${round2(nsTags)} ns")
            appendLine("  ProfileData.avatarCdnImage -> CdnImage      : ${round2(nsAvatar)} ns   [USED]")
            appendLine("  ProfileData.bannerCdnImage -> CdnImage      : ${round2(nsBanner)} ns   [WASTED]")
            appendLine("  ProfileData.aboutUris -> List<String>       : ${round2(nsAboutUris)} ns   [WASTED]")
            appendLine("  ProfileData.aboutHashtags -> List<String>   : ${round2(nsAboutTags)} ns   [WASTED]")
            appendLine("  ProfileData.blossoms -> List<String>        : ${round2(nsBlossoms)} ns   [USED]")
            appendLine("  ProfileData.primalPremiumInfo -> object     : ${round2(nsPremium)} ns   [USED]")
            appendLine("  ProfileData.raw -> NostrEvent (passthrough) : ${round2(nsProfileRaw)} ns")
            appendLine("-----------------------------------------------------------------------------------")
            appendLine(
                "Per-note: main row = ${round2(
                    nsMainRow / 1000.0,
                )} us ; profile(all 6) = ${round2(nsProfileAll / 1000.0)} us",
            )
            appendLine("  profile USED(avatar+premium+blossoms) = ${round2(nsProfileUsed / 1000.0)} us")
            appendLine("  profile WASTED(banner+aboutUris+aboutTags) = ${round2(nsProfileWasted / 1000.0)} us")
            appendLine("-----------------------------------------------------------------------------------")
            appendLine("PER REFRESH PAGE ($pageNotes notes):")
            appendLine("  total decode, author profile x1 : ${round2(msPageX1)} ms")
            appendLine("  total decode, profile x3        : ${round2(msPageX3)} ms")
            appendLine("  >> WASTED (discarded profile JSON): ${round2(msWastedPage)} ms / page")
            appendLine("  >> rawKind full-event decode only : ${round2(msRawKindPage)} ms / page (just to read .kind)")
            appendLine("  >> ProfileData.raw passthrough    : ${round2(msProfileRawPage)} ms / page")
            appendLine("NOTE: this whole cost is RE-PAID on every PagingSource invalidation (Problem A).")
            appendLine("===================================================================================")
        }
        println(report)
    }
}
