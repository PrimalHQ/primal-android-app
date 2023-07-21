package net.primal.android.nostr.ext

import net.primal.android.crypto.bechToBytes
import net.primal.android.crypto.toHex
import net.primal.android.feed.db.Nip19Entity
import net.primal.android.feed.db.PostData
import net.primal.android.profile.db.ProfileMetadata

fun List<PostData>.flatMapAsPostNip19Entities(mapAsProfileMetadata: Map<String, ProfileMetadata>) =
    flatMap { postData ->
        postData.nip19Links
            .filter { it.startsWith(NOSTR + NPUB) || it.startsWith(NOSTR + NPROFILE) }
            .map { link ->

                val profileMetadata: ProfileMetadata?
                var profileId: String? = null
                var displayName: String? = null

                val matcher = NIP19_REGEXP.matcher(link)
                if (matcher.find()) {

//                    val uriScheme = matcher.group(1) // nostr:
                    val type = matcher.group(2) // npub1
                    val key = matcher.group(3) // bech32
//                    val additionalChars = matcher.group(4)
                    try {
                        when (type.lowercase()) {
                            NPUB -> {
                                profileId = (type + key).bechToBytes().toHex()
                            }

                            NPROFILE -> {
                                val tlv = Nip19Tlv.parse((type + key).bechToBytes())

                                profileId = tlv.get(Nip19Tlv.Type.SPECIAL.id)
                                    ?.get(0)
                                    ?.toHex()
                            }
                        }
                        profileMetadata = mapAsProfileMetadata[profileId]
                        if (profileMetadata?.displayName.isNullOrEmpty()) {
                            displayName = profileMetadata?.name
                        } else {
                            displayName = profileMetadata?.displayName
                        }
                    } catch (error: IllegalArgumentException) {
                        // ignore invalid links
                    }
                }
                Nip19Entity(
                    eventId = postData.postId,
                    link = link,
                    profileId = profileId,
                    displayName = displayName,
                )
            }
    }
