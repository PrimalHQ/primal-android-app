package net.primal.android.user.accounts

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.serialization.NostrJson
import net.primal.android.user.domain.Relay


fun String.parseRelays(): List<Relay> {
    val jsonContent = try {
        NostrJson.parseToJsonElement(this)
    } catch (error: SerializationException) {
        null
    }

    val relays = mutableListOf<Relay>()
    jsonContent?.jsonObject?.entries?.forEach {
        val relayUrl = it.key
        val permissions = it.value.jsonObject
        val read = permissions["read"]?.jsonPrimitive?.boolean ?: false
        val write = permissions["write"]?.jsonPrimitive?.boolean ?: false
        relays.add(Relay(url = relayUrl, read = read, write = write))
    }

    return relays
}

fun List<JsonArray>.parseFollowings(): List<String> {
    val followings = mutableListOf<String>()
    this.forEach {
        if (it[0].jsonPrimitive.content == "p") {
            val pubkey = it[1].jsonPrimitive.content
            followings.add(pubkey)
        }
    }
    return followings
}

fun List<JsonArray>.parseInterests(): List<String> {
    val interests = mutableListOf<String>()
    this.forEach {
        if (it[0].jsonPrimitive.content == "t") {
            val tag = it[1].jsonPrimitive.content
            interests.add(tag)
        }
    }
    return interests
}
