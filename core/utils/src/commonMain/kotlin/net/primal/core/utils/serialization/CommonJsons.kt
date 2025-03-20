package net.primal.core.utils.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder

private val defaultJsonBuilder: (JsonBuilder.() -> Unit) = {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

val CommonJson = Json {
    defaultJsonBuilder()
}

val CommonJsonImplicitNulls = Json {
    defaultJsonBuilder()
    explicitNulls = false
}
