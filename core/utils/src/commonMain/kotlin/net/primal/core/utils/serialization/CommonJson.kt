package net.primal.core.utils.serialization

import kotlinx.serialization.json.Json

val CommonJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
