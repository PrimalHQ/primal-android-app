package net.primal.android.nostr.ext

import net.primal.android.crypto.bechToBytes
import net.primal.android.crypto.toHex
import net.primal.android.feed.db.NostrUri
import net.primal.android.feed.db.PostData
import net.primal.android.profile.db.ProfileMetadata
import net.primal.android.profile.db.displayNameUiFriendly

fun List<PostData>.flatMapAsPostNostrUris(mapAsProfileMetadata: Map<String, ProfileMetadata>) =
    flatMap { postData ->
        postData.uris
            .filter { it.isNostrUri() }
            .map { link ->

                var profileId: String? = null
                var noteId: String? = null
                var name: String? = null

                val matcher = NIP19_REGEXP.matcher(link)
                if (matcher.find()) {

                    val type = matcher.group(2) // npub1
                    val key = matcher.group(3) // bech32
                    try {
                        when (type.lowercase()) {
                            NPUB -> {
                                profileId = (type + key).bechToBytes().toHex()
                                name = mapAsProfileMetadata[profileId]?.displayNameUiFriendly()
                            }

                            NOTE -> {
                                noteId = (type + key).bechToBytes().toHex()
                            }

                            NEVENT -> {
                                val tlv = Nip19Tlv.parse((type + key).bechToBytes())

                                noteId = tlv.get(Nip19Tlv.Type.SPECIAL.id)
                                    ?.get(0)
                                    ?.toHex()
                            }

                            NPROFILE -> {
                                val tlv = Nip19Tlv.parse((type + key).bechToBytes())

                                profileId = tlv.get(Nip19Tlv.Type.SPECIAL.id)
                                    ?.get(0)
                                    ?.toHex()
                                name = mapAsProfileMetadata[profileId]?.displayNameUiFriendly()
                            }

                            else -> {
                                // unsupported entity
                                NostrUri(
                                    eventId = postData.postId,
                                    uri = link,
                                    profileId = null,
                                    noteId = null,
                                    name = null,
                                )
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

