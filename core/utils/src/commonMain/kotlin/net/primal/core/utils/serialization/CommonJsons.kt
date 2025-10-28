package net.primal.core.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder

private val defaultJsonBuilder: (JsonBuilder.() -> Unit) = {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

@OptIn(ExperimentalSerializationApi::class)
val CommonJson = Json {
    defaultJsonBuilder()
    classDiscriminatorMode = ClassDiscriminatorMode.NONE
}

val CommonJsonImplicitNulls = Json {
    defaultJsonBuilder()
    explicitNulls = false
}

val CommonJsonEncodeDefaults = Json {
    defaultJsonBuilder()
    encodeDefaults = true
}
