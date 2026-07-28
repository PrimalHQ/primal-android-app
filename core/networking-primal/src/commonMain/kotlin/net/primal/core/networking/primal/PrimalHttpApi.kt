package net.primal.core.networking.primal

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Url
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

/**
 * Raw HTTP transport for Primal servers. Prefer [PrimalHttpApiClient].
 *
 * The api answers with `HTTP 200` regardless of the outcome, returning either a `JsonArray` of
 * result events or a `JsonObject` describing the error, hence the raw [JsonElement] response.
 */
interface PrimalHttpApi {

    @POST
    suspend fun request(@Url url: String, @Body body: JsonArray): JsonElement
}
