package net.primal.android.wallet.activation.regions

import kotlinx.serialization.Serializable

@Serializable
data class Regions(
    val countries: List<List<String>>,
    val states: List<List<String>>,
)

fun Regions.toListOfCountries(): List<Country> {
    return countries.map { country ->
        val countryName = country[0]
        val countryCode = country[1]
        Country(
            name = countryName,
            code = countryCode,
            states = states.mapNotNull { state ->
                val stateName = state[0]
                val stateCode = state[1]
                if (stateCode.startsWith(countryCode)) {
                    State(name = stateName, code = stateCode)
                } else {
                    null
                }
            },
        )
    }
}

sealed class Region(
    open val name: String,
    open val code: String,
)

data class Country(
    override val name: String,
    override val code: String,
    val states: List<State>,
) : Region(name, code)

data class State(
    override val name: String,
    override val code: String,
) : Region(name, code)
