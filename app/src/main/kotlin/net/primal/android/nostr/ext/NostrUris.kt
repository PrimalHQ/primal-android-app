package net.primal.android.nostr.ext

import net.primal.android.crypto.bechToBytes
import net.primal.android.crypto.toHex
import net.primal.android.feed.db.NostrUri
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.utils.Nip19TLV
import java.util.regex.Pattern

private const val NOSTR = "nostr:"
private const val NPUB = "npub1"
private const val NSEC = "nsec1"
private const val NEVENT = "nevent1"
private const val NADDR = "naddr1"
private const val NOTE = "note1"
private const val NRELAY = "nrelay1"
private const val NPROFILE = "nprofile1"

private val nostrUriRegexPattern: Pattern = Pattern.compile(
    "($NOSTR)?@?($NSEC|$NPUB|$NEVENT|$NADDR|$NOTE|$NPROFILE|$NRELAY)([qpzry9x8gf2tvdw0s3jn54khce6mua7l]+)([\\S]*)",
    Pattern.CASE_INSENSITIVE
)

fun String.isNostrUri(): Boolean {
    return this.lowercase().startsWith(NOSTR)
}

fun String.parseNostrUris(): List<String> {
    return nostrUriRegexPattern.toRegex().findAll(this).map { matchResult ->
        matchResult.groupValues[1] + matchResult.groupValues[2] + matchResult.groupValues[3]
    }.toList()
}

fun List<PostData>.flatMapAsPostNostrUri(profileIdToDisplayNameMap: Map<String, String>) =
    flatMap { postData ->
        postData.uris
            .filter { it.isNostrUri() }
            .map { link ->
                var profileId: String? = null
                var noteId: String? = null
                var name: String? = null

                val matcher = nostrUriRegexPattern.matcher(link)
                if (matcher.find()) {
                    val type = matcher.group(2) // npub1
                    val key = matcher.group(3) // bech32
                    try {
                        when (type?.lowercase()) {
                            NPUB -> {
                                profileId = (type + key).bechToBytes().toHex()
                                name = profileIdToDisplayNameMap[profileId]
                            }

                            NOTE -> {
                                noteId = (type + key).bechToBytes().toHex()
                            }

                            NEVENT -> {
                                val tlv = Nip19TLV.parse((type + key).bechToBytes())
                                noteId = tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
                            }

                            NPROFILE -> {
                                val tlv = Nip19TLV.parse((type + key).bechToBytes())
                                profileId = tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
                                name = profileIdToDisplayNameMap[profileId]
                            }

                            else -> {
                                NostrUri(eventId = postData.postId, uri = link)
                            }

                        }
                    } catch (error: IllegalArgumentException) {
                        // ignore invalid links
                    }
                }
                NostrUri(
                    eventId = postData.postId,
                    uri = link,
                    profileId = profileId,
                    noteId = noteId,
                    name = name,
                )
            }
    }
